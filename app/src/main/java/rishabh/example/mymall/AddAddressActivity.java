package rishabh.example.mymall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddAddressActivity extends AppCompatActivity {

    private Button saveBtn;
    private EditText city, locality, flatNo, pinCode, landMark, name, mobileNo, alternateMobileNo;
    private Spinner stateSpinner;

    private String selectedState;
    private String[] stateList;
    private Dialog loadingDialog;

    private boolean updateAddress = false;
    private AddressesModel addressesModel;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        ////loading dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ////loading dialog

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        saveBtn = findViewById(R.id.save_btn);
        city = findViewById(R.id.city);
        locality = findViewById(R.id.locality);
        flatNo = findViewById(R.id.flat_no);
        pinCode = findViewById(R.id.pincode);
        landMark = findViewById(R.id.landmark);
        name = findViewById(R.id.name);
        mobileNo = findViewById(R.id.mobile_no);
        alternateMobileNo = findViewById(R.id.alternate_mobile_no);
        stateSpinner = findViewById(R.id.stateSpinner);

        stateList = getResources().getStringArray(R.array.india_states);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Add New Address");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ArrayAdapter spinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, stateList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        stateSpinner.setAdapter(spinnerAdapter);

        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedState = stateList[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (getIntent().getStringExtra("INTENT").equals("update_address")) {
            updateAddress = true;
            position = getIntent().getIntExtra("index", -1);
            addressesModel = DBQueries.addressesModelList.get(position);

            city.setText(addressesModel.getCity());
            locality.setText(addressesModel.getLocality());
            flatNo.setText(addressesModel.getFlatNo());
            landMark.setText(addressesModel.getLandMark());
            name.setText(addressesModel.getName());
            mobileNo.setText(addressesModel.getMobileNo());
            alternateMobileNo.setText(addressesModel.getAlternateMobileNo());
            pinCode.setText(addressesModel.getPinCode());
            for (int i = 0; i < stateList.length; i++) {
                if (stateList[i].equals(addressesModel.getState())) {
                    stateSpinner.setSelection(i);
                }
            }
            saveBtn.setText("Update");
        } else {
            position = DBQueries.addressesModelList.size();
        }

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(city.getText())) {
                    if (!TextUtils.isEmpty(locality.getText())) {
                        if (!TextUtils.isEmpty(flatNo.getText())) {
                            if (!TextUtils.isEmpty(pinCode.getText()) && pinCode.getText().length() == 6) {
                                if (!TextUtils.isEmpty(name.getText())) {
                                    if (!TextUtils.isEmpty(mobileNo.getText()) && mobileNo.getText().length() == 10) {

                                        loadingDialog.show();

                                        Map<String, Object> addAddress = new HashMap();
                                        addAddress.put("city_" + String.valueOf(position + 1), city.getText().toString());
                                        addAddress.put("locality_" + String.valueOf(position + 1), locality.getText().toString());
                                        addAddress.put("flat_no_" + String.valueOf(position + 1), flatNo.getText().toString());
                                        addAddress.put("pincode_" + String.valueOf(position + 1), pinCode.getText().toString());
                                        addAddress.put("landmark_" + String.valueOf(position + 1), landMark.getText().toString());
                                        addAddress.put("name_" + String.valueOf(position + 1), name.getText().toString());
                                        addAddress.put("mobile_no_" + String.valueOf(position + 1), mobileNo.getText().toString());
                                        addAddress.put("alternate_mobile_no_" + String.valueOf(position + 1), alternateMobileNo.getText().toString());
                                        addAddress.put("state_" + String.valueOf(position + 1), selectedState);
                                        if (!updateAddress) {
                                            addAddress.put("list_size", (long) DBQueries.addressesModelList.size() + 1);
                                            if (getIntent().getStringExtra("INTENT").equals("manage")) {
                                                if (DBQueries.addressesModelList.size() == 0) {
                                                    addAddress.put("selected_" + String.valueOf(position + 1), true);
                                                } else {
                                                    addAddress.put("selected_" + String.valueOf(position + 1), false);
                                                }
                                            } else {
                                                addAddress.put("selected_" + String.valueOf(position + 1), true);
                                            }
                                            if (DBQueries.addressesModelList.size() > 0) {
                                                addAddress.put("selected_" + (DBQueries.selectedAddress + 1), false);
                                            }
                                        }

                                        FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid())
                                                .collection("USER_DATA").document("MY_ADDRESSES")
                                                .update(addAddress).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    if (!updateAddress) {
                                                        if (DBQueries.addressesModelList.size() > 0) {
                                                            DBQueries.addressesModelList.get(DBQueries.selectedAddress).setSelected(false);
                                                        }
                                                        DBQueries.addressesModelList.add(new AddressesModel(true, city.getText().toString(), locality.getText().toString(), flatNo.getText().toString(), pinCode.getText().toString(), landMark.getText().toString(), name.getText().toString(), mobileNo.getText().toString(), alternateMobileNo.getText().toString(), selectedState));

                                                        if (getIntent().getStringExtra("INTENT").equals("manage")) {
                                                            if (DBQueries.addressesModelList.size() == 0) {
                                                                DBQueries.selectedAddress = DBQueries.addressesModelList.size() - 1;
                                                            }
                                                        } else {
                                                            DBQueries.selectedAddress = DBQueries.addressesModelList.size() - 1;
                                                        }
                                                    } else {
                                                        DBQueries.addressesModelList.set(position, new AddressesModel(true, city.getText().toString(), locality.getText().toString(), flatNo.getText().toString(), pinCode.getText().toString(), landMark.getText().toString(), name.getText().toString(), mobileNo.getText().toString(), alternateMobileNo.getText().toString(), selectedState));
                                                    }
                                                    if (getIntent().getStringExtra("INTENT").equals("deliveryIntent")) {
                                                        Intent deliveryIntent = new Intent(AddAddressActivity.this, DeliveryActivity.class);
                                                        startActivity(deliveryIntent);
                                                    } else {
                                                        MyAddressesActivity.refreshItem(DBQueries.selectedAddress, DBQueries.addressesModelList.size() - 1);
                                                    }
                                                    finish();

                                                } else {
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(AddAddressActivity.this, error, Toast.LENGTH_SHORT).show();
                                                }
                                                loadingDialog.dismiss();
                                            }
                                        });

                                    } else {
                                        mobileNo.requestFocus();
                                        Toast.makeText(AddAddressActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    name.requestFocus();
                                }
                            } else {
                                pinCode.requestFocus();
                                Toast.makeText(AddAddressActivity.this, "Invalid PIN Code", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            flatNo.requestFocus();
                        }
                    } else {
                        locality.requestFocus();
                    }
                } else {
                    city.requestFocus();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}