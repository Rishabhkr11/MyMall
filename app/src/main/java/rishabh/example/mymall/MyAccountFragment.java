package rishabh.example.mymall;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyAccountFragment extends Fragment {

    public MyAccountFragment() {
        // Required empty public constructor
    }

    public static final int MANAGE_ADDRESS = 1;
    private Button viewAllAddressBtn, signOutBtn;
    private CircleImageView profileView, currentOrderImage;
    private TextView name, email, tvCurrentOrderStatus;
    private LinearLayout layoutContainer, recentOrdersContainer;
    private Dialog loadingDialog;
    private ImageView orderedIndicator, packedIndicator, shippedIndicator, deliveredIndicator;
    private ProgressBar O_P_progress, P_S_progress, S_D_progress;
    private TextView yourRecentOrdersTitle;
    private TextView addressName, address, pinCode;
    private FloatingActionButton settingsBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_my_account, container, false);

        viewAllAddressBtn = view.findViewById(R.id.view_all_addresses_btn);
        profileView = view.findViewById(R.id.profile_image);
        name = view.findViewById(R.id.user_name);
        email = view.findViewById(R.id.user_email);
        layoutContainer = view.findViewById(R.id.layout_container);
        currentOrderImage = view.findViewById(R.id.current_order_image);
        tvCurrentOrderStatus = view.findViewById(R.id.tv_current_order_status);
        orderedIndicator = view.findViewById(R.id.ordered_indicator);
        packedIndicator = view.findViewById(R.id.packed_indicator);
        shippedIndicator = view.findViewById(R.id.shipped_indicator);
        deliveredIndicator = view.findViewById(R.id.delivered_indicator);
        O_P_progress = view.findViewById(R.id.order_packed_progress);
        P_S_progress = view.findViewById(R.id.packed_shipped_progress);
        S_D_progress = view.findViewById(R.id.shipped_delivered_progress);
        yourRecentOrdersTitle = view.findViewById(R.id.your_recent_orders_title);
        recentOrdersContainer = view.findViewById(R.id.recent_orders_container);
        addressName = view.findViewById(R.id.address_full_name);
        address = view.findViewById(R.id.address_profile);
        pinCode = view.findViewById(R.id.address_pincode);
        signOutBtn = view.findViewById(R.id.sign_out_btn);
        settingsBtn = view.findViewById(R.id.settings_btn);

        ////loading dialog
        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.show();
        ////loading dialog

        DBQueries.loadOrders(getContext(), null, loadingDialog);

        layoutContainer.getChildAt(1).setVisibility(View.GONE);
        loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                for (MyOrderItemModel orderItemModel : DBQueries.myOrderItemModelList) {
                    if (!orderItemModel.isCancellationRequested()) {
                        if (!orderItemModel.getOrderStatus().equals("Delivered") && !orderItemModel.getOrderStatus().equals("Cancelled")) {
                            layoutContainer.getChildAt(1).setVisibility(View.VISIBLE);
                            Glide.with(getContext()).load(orderItemModel.getProductImage()).apply(new RequestOptions().placeholder(R.mipmap.icon_place_holder)).into(currentOrderImage);
                            tvCurrentOrderStatus.setText(orderItemModel.getOrderStatus());

                            switch (orderItemModel.getOrderStatus()) {
                                case "Ordered":
                                    orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                    break;

                                case "Packed":
                                    orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                    packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                    O_P_progress.setProgress(100);
                                    break;

                                case "Shipped":
                                    orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                    packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                    shippedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                    O_P_progress.setProgress(100);
                                    P_S_progress.setProgress(100);
                                    break;

                                case "Out for Delivery":
                                    orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                    packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                    shippedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                    deliveredIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                                    O_P_progress.setProgress(100);
                                    P_S_progress.setProgress(100);
                                    S_D_progress.setProgress(100);
                                    break;
                            }
                        }
                    }
                }
                int i = 0;
                for (MyOrderItemModel myOrderItemModel : DBQueries.myOrderItemModelList) {
                    if (i < 4) {
                        if (myOrderItemModel.getOrderStatus().equals("Delivered")) {
                            Glide.with(getContext()).load(myOrderItemModel.getProductImage()).apply(new RequestOptions().placeholder(R.mipmap.icon_place_holder)).into((CircleImageView) recentOrdersContainer.getChildAt(i));
                            i++;
                        }
                    } else {
                        break;
                    }
                }
                if (i == 0) {
                    yourRecentOrdersTitle.setText("No Recent Orders");
                }
                if (i < 3) {
                    for (int x = i; x < 4; x++) {
                        recentOrdersContainer.getChildAt(x).setVisibility(View.GONE);
                    }
                }
                loadingDialog.show();
                loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        loadingDialog.setOnDismissListener(null);
                        if (DBQueries.addressesModelList.size() == 0) {
                            addressName.setText("No Address");
                            address.setText("-");
                            pinCode.setText("-");
                        } else {
                            setAddress();
                        }
                    }
                });
                DBQueries.loadAddresses(getContext(), loadingDialog, false);
            }
        });

        viewAllAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addressIntent = new Intent(getContext(), MyAddressesActivity.class);
                addressIntent.putExtra("MODE", MANAGE_ADDRESS);
                startActivity(addressIntent);
            }
        });

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                DBQueries.clearData();
                Intent intent = new Intent(getContext(), RegisterActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent updateUserInfo = new Intent(getContext(), UpdateUserInfoActivity.class);
                updateUserInfo.putExtra("Name", name.getText());
                updateUserInfo.putExtra("Email", email.getText());
                updateUserInfo.putExtra("Photo", DBQueries.profile);
                startActivity(updateUserInfo);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        name.setText(DBQueries.fullName);
        email.setText(DBQueries.email);

        if (!DBQueries.profile.equals("")) {
            Glide.with(getContext()).load(DBQueries.profile).apply(new RequestOptions().placeholder(R.mipmap.profile_placeholder)).into(profileView);
        }else {
            profileView.setImageResource(R.mipmap.profile_placeholder);
        }

        if (!loadingDialog.isShowing()){
            if (DBQueries.addressesModelList.size() == 0) {
                addressName.setText("No Address");
                address.setText("-");
                pinCode.setText("-");
            } else {
                setAddress();
            }
        }
    }

    private void setAddress() {
        String nameText, mobileNo, alternateMobileNo;
        nameText = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getName();
        mobileNo = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getMobileNo();
        alternateMobileNo = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getAlternateMobileNo();
        if (DBQueries.addressesModelList.get(DBQueries.selectedAddress).getAlternateMobileNo().equals("")) {
            addressName.setText(nameText + " - " + mobileNo);
        }
        else {
            addressName.setText(nameText + " - " + mobileNo + " or " + alternateMobileNo);
        }
        String flatNo = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getFlatNo();
        String locality = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getLocality();
        String landmark = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getLandMark();
        String city = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getCity();
        String state = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getState();

        if (landmark.equals("")){
            address.setText(flatNo + ", " + locality + ", " + city + ", " + state);
        }else {
            address.setText(flatNo + ", " + locality + ", " + landmark + ", " + city + ", " + state);
        }
        pinCode.setText(DBQueries.addressesModelList.get(DBQueries.selectedAddress).getPinCode());
    }
}