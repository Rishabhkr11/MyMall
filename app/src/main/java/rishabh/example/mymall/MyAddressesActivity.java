package rishabh.example.mymall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static rishabh.example.mymall.DeliveryActivity.SELECT_ADDRESS;

public class MyAddressesActivity extends AppCompatActivity {

    private RecyclerView myAddressesRecyclerView;
    private static AddresseAdapter addresseAdapter;
    private Button deliverhereBtn;
    private LinearLayout addNewAddressBtn;
    private TextView addressesSaved;
    private int previousAddress;
    private Dialog loadingDialog;
    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_addresses);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        myAddressesRecyclerView = findViewById(R.id.addresses_recycler_view);
        deliverhereBtn = findViewById(R.id.deliver_here_btn);
        addNewAddressBtn = findViewById(R.id.add_new_address_btn);
        addressesSaved = findViewById(R.id.address_saved);
        previousAddress = DBQueries.selectedAddress;

        ////loading dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                addressesSaved.setText(String.valueOf(DBQueries.addressesModelList.size()) + " Saved Addresses");
            }
        });
        ////loading dialog

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("My Addresses");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        myAddressesRecyclerView.setLayoutManager(layoutManager);

        mode = getIntent().getIntExtra("MODE", -1);
        if (mode == SELECT_ADDRESS) {
            deliverhereBtn.setVisibility(View.VISIBLE);
        } else {
            deliverhereBtn.setVisibility(View.GONE);
        }

        deliverhereBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DBQueries.selectedAddress != previousAddress) {
                    final int previousAddressIndex = previousAddress;
                    loadingDialog.show();

                    Map<String, Object> updateSelection = new HashMap<>();
                    updateSelection.put("selected_" + String.valueOf(previousAddress + 1), false);
                    updateSelection.put("selected_" + String.valueOf(DBQueries.selectedAddress + 1), true);

                    previousAddress = DBQueries.selectedAddress;

                    FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid())
                            .collection("USER_DATA").document("MY_ADDRESSES").update(updateSelection).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                finish();
                            } else {
                                previousAddress = previousAddressIndex;
                                String error = task.getException().getMessage();
                                Toast.makeText(MyAddressesActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                            loadingDialog.dismiss();
                        }
                    });
                } else {
                    finish();
                }
            }
        });

        addresseAdapter = new AddresseAdapter(DBQueries.addressesModelList, mode, loadingDialog);
        myAddressesRecyclerView.setAdapter(addresseAdapter);
        ((SimpleItemAnimator) myAddressesRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        addresseAdapter.notifyDataSetChanged();

        addNewAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addAddressIntent = new Intent(MyAddressesActivity.this, AddAddressActivity.class);
                if (mode != SELECT_ADDRESS){
                    addAddressIntent.putExtra("INTENT", "manage");
                }else {
                    addAddressIntent.putExtra("INTENT", "null");
                }
                startActivity(addAddressIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        addressesSaved.setText(String.valueOf(DBQueries.addressesModelList.size()) + " Saved Addresses");
    }

    public static void refreshItem(int deselect, int select) {
        addresseAdapter.notifyItemChanged(deselect);
        addresseAdapter.notifyItemChanged(select);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (mode == SELECT_ADDRESS) {
                if (DBQueries.selectedAddress != previousAddress) {
                    DBQueries.addressesModelList.get(DBQueries.selectedAddress).setSelected(false);
                    DBQueries.addressesModelList.get(previousAddress).setSelected(true);
                    DBQueries.selectedAddress = previousAddress;
                }
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mode == SELECT_ADDRESS) {
            if (DBQueries.selectedAddress != previousAddress) {
                DBQueries.addressesModelList.get(DBQueries.selectedAddress).setSelected(false);
                DBQueries.addressesModelList.get(previousAddress).setSelected(true);
                DBQueries.selectedAddress = previousAddress;
            }
        }
        super.onBackPressed();
    }
}