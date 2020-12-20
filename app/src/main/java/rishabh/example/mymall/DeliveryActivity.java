package rishabh.example.mymall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeliveryActivity extends AppCompatActivity implements PaymentResultListener {

    private RecyclerView deliveryRecyclerView;
    private Button changeAddressBtn, continueBtn;
    public static final int SELECT_ADDRESS = 0;
    private TextView totalAmount, fullName, fullAddress, pinCode;
    private String name, mobileNo;
    public static List<CartItemModel> cartItemModelList;
    private Dialog paymentMethodDialog;
    private TextView codTitle;
    private View divider;
    public static Dialog loadingDialog;
    private ImageView paytm, cod;
    private String paymentMethod = "PAYTM";
    private ConstraintLayout orderConfirmationLayout;
    private ImageButton continueShoppingBtn;
    private TextView orderId;
    private String order_id;
    private boolean successResponce = false;
    public static boolean fromCart;
    public static boolean codOrderConfirmed = false;

    private FirebaseFirestore firebaseFirestore;
    public static CartAdapter cartAdapter;
    public static boolean getQtyIDs = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Checkout.preload(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Delivery");

        deliveryRecyclerView = findViewById(R.id.delivery_recycler_view);
        changeAddressBtn = findViewById(R.id.change_or_add_address_btn);
        totalAmount = findViewById(R.id.total_cart_amount);
        fullName = findViewById(R.id.fullname);
        fullAddress = findViewById(R.id.address);
        pinCode = findViewById(R.id.pincode);
        continueBtn = findViewById(R.id.cart_continue_btn);
        orderConfirmationLayout = findViewById(R.id.order_confirmation_layout);
        continueShoppingBtn = findViewById(R.id.continue_shopping_btn);
        orderId = findViewById(R.id.order_id);

        ////loading dialog
        loadingDialog = new Dialog(DeliveryActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ////loading dialog

        ////paymentMethod dialog
        paymentMethodDialog = new Dialog(DeliveryActivity.this);
        paymentMethodDialog.setContentView(R.layout.payment_method);
        paymentMethodDialog.setCancelable(true);
        paymentMethodDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        paymentMethodDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paytm = paymentMethodDialog.findViewById(R.id.paytm);
        cod = paymentMethodDialog.findViewById(R.id.cod_btn);
        codTitle = paymentMethodDialog.findViewById(R.id.cod_btn_title);
        divider = paymentMethodDialog.findViewById(R.id.divider_cod);
        ////paymentMethod dialog

        order_id = UUID.randomUUID().toString().substring(0, 28);
        firebaseFirestore = FirebaseFirestore.getInstance();
        getQtyIDs = true;

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        deliveryRecyclerView.setLayoutManager(layoutManager);

        cartAdapter = new CartAdapter(cartItemModelList, totalAmount, false);
        deliveryRecyclerView.setAdapter(cartAdapter);
        cartAdapter.notifyDataSetChanged();

        changeAddressBtn.setVisibility(View.VISIBLE);

        changeAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getQtyIDs = false;
                Intent addressIntent = new Intent(DeliveryActivity.this, MyAddressesActivity.class);
                addressIntent.putExtra("MODE", SELECT_ADDRESS);
                startActivity(addressIntent);
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean allProductsAvailable = true;
                for (CartItemModel cartItemModel : cartItemModelList) {
                    if (cartItemModel.isQtyError()) {
                        allProductsAvailable = false;
                        break;
                    }
                    if (cartItemModel.getType() == CartItemModel.CART_ITEM) {
                        if (!cartItemModel.isCOD()) {
                            cod.setEnabled(false);
                            cod.setAlpha(0.5f);
                            codTitle.setAlpha(0.5f);
                            break;
                        } else {
                            cod.setEnabled(true);
                            cod.setAlpha(1f);
                            codTitle.setAlpha(1f);
                        }
                    }
                }
                if (allProductsAvailable) {
                    paymentMethodDialog.show();
                }
            }
        });

        cod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentMethod = "COD";
                placeOrderDetails();
            }
        });

        paytm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentMethod = "PAYTM";
                placeOrderDetails();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        /////accessing quantity
        if (getQtyIDs) {
            loadingDialog.show();
            for (int x = 0; x < cartItemModelList.size() - 1; x++) {

                for (int y = 0; y < cartItemModelList.get(x).getProductQuantity(); y++) {
                    final String quantityDocumentName = UUID.randomUUID().toString().substring(0, 20);

                    Map<String, Object> timestamp = new HashMap<>();
                    timestamp.put("time", FieldValue.serverTimestamp());
                    final int finalX = x;
                    final int finalY = y;
                    firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(x).getProductID()).collection("QUANTITY").document(quantityDocumentName).set(timestamp)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        cartItemModelList.get(finalX).getQtyIDs().add(quantityDocumentName);
                                        if (finalY + 1 == cartItemModelList.get(finalX).getProductQuantity()) {
                                            firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(finalX).getProductID()).collection("QUANTITY").orderBy("time", Query.Direction.ASCENDING).limit(cartItemModelList.get(finalX).getStockQuantity()).get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()) {

                                                                List<String> serverQuantity = new ArrayList<>();

                                                                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                                                                    serverQuantity.add(queryDocumentSnapshot.getId());

                                                                }
                                                                long availableQty = 0;
                                                                boolean noLongerAvailable = true;
                                                                for (String qtyId : cartItemModelList.get(finalX).getQtyIDs()) {
                                                                    cartItemModelList.get(finalX).setQtyError(false);
                                                                    if (!serverQuantity.contains(qtyId)) {
                                                                        if (noLongerAvailable) {
                                                                            cartItemModelList.get(finalX).setInStock(false);
                                                                        } else {
                                                                            cartItemModelList.get(finalX).setQtyError(true);
                                                                            cartItemModelList.get(finalX).setMaxQuantity(availableQty);
                                                                            Toast.makeText(DeliveryActivity.this, "Sorry! All products may not be available in required quantity", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    } else {
                                                                        availableQty++;
                                                                        noLongerAvailable = false;
                                                                    }
                                                                }
                                                                cartAdapter.notifyDataSetChanged();
                                                            } else {
                                                                String error = task.getException().getMessage();
                                                                Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                                                            }
                                                            loadingDialog.dismiss();
                                                        }
                                                    });
                                        }

                                    } else {
                                        loadingDialog.dismiss();
                                        String error = task.getException().getMessage();
                                        Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        } else {
            getQtyIDs = true;
        }
        /////accessing quantity

        name = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getName();
        mobileNo = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getMobileNo();
        if (DBQueries.addressesModelList.get(DBQueries.selectedAddress).getAlternateMobileNo().equals("")) {
            fullName.setText(name + " - " + mobileNo);
        }
        else {
            fullName.setText(name + " - " + mobileNo + " or " + DBQueries.addressesModelList.get(DBQueries.selectedAddress).getAlternateMobileNo());
        }
        String flatNo = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getFlatNo();
        String locality = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getLocality();
        String landmark = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getLandMark();
        String city = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getCity();
        String state = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getState();

        if (landmark.equals("")){
            fullAddress.setText(flatNo + ", " + locality + ", " + city + ", " + state);
        }else {
            fullAddress.setText(flatNo + ", " + locality + ", " + landmark + ", " + city + ", " + state);
        }
        pinCode.setText(DBQueries.addressesModelList.get(DBQueries.selectedAddress).getPinCode());

        if (codOrderConfirmed) {
            showConfirmationLayout();
        }
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

    @Override
    protected void onPause() {
        super.onPause();
        loadingDialog.dismiss();

        if (getQtyIDs) {
            for (int x = 0; x < cartItemModelList.size() - 1; x++) {
                if (!successResponce) {
                    for (final String qtyID : cartItemModelList.get(x).getQtyIDs()) {
                        final int finalX = x;
                        firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(x).getProductID()).collection("QUANTITY").document(qtyID).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (qtyID.equals(cartItemModelList.get(finalX).getQtyIDs().get(cartItemModelList.get(finalX).getQtyIDs().size() - 1))) {
                                            cartItemModelList.get(finalX).getQtyIDs().clear();
                                        }
                                    }
                                });

                    }
                } else {
                    cartItemModelList.get(x).getQtyIDs().clear();
                }
            }
        }
    }

    @Override
    public void onPaymentSuccess(String s) {

        Map<String, Object> updateStatus = new HashMap<>();
        updateStatus.put("Payment Status", "Paid");
        updateStatus.put("Order Status", "Ordered");
        firebaseFirestore.collection("ORDERS").document(order_id).update(updateStatus)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> userOrder = new HashMap<>();
                            userOrder.put("order_id", order_id);
                            userOrder.put("time", FieldValue.serverTimestamp());
                            firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_ORDERS").document(order_id).set(userOrder)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                showConfirmationLayout();
                                            } else {
                                                Toast.makeText(DeliveryActivity.this, "Failed to update User Order List", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(DeliveryActivity.this, "Order Cancelled", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onPaymentError(int i, String s) {
        loadingDialog.dismiss();
        Toast.makeText(this, "Payment Failed : " + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (successResponce = true) {
            finish();
            return;
        }
        super.onBackPressed();
    }

    private void showConfirmationLayout() {
        successResponce = true;
        codOrderConfirmed = false;
        getQtyIDs = false;

        for (int x = 0; x < cartItemModelList.size() - 1; x++) {

            for (String qtyID : cartItemModelList.get(x).getQtyIDs()) {

                firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(x).getProductID()).collection("QUANTITY").document(qtyID).update("user_ID", FirebaseAuth.getInstance().getUid());
            }
        }

        if (MainActivity.mainActivity != null) {
            MainActivity.mainActivity.finish();
            MainActivity.mainActivity = null;
            MainActivity.showCart = false;
        } else {
            MainActivity.resetMainActivity = true;
        }

        if (ProductDetailsActivity.productDetailsActivity != null) {
            ProductDetailsActivity.productDetailsActivity.finish();
            ProductDetailsActivity.productDetailsActivity = null;
        }

        /////sent confirmation sms
        String SMS_API = "https://www.fast2sms.com/dev/bulk";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SMS_API, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                /////nothing
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                /////nothing
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("authorization", "bnsiSqpOfADJXcmI438Hg0BQaRoNLGjl6wUYyrCPWu1xe2v5EMO51KU8GeTPLbSZQ7i6Dz2RI9qufFwt");

                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> body = new HashMap<>();
                body.put("sender_id", "FSTSMS");
                body.put("language", "english");
                body.put("route", "qt");
                body.put("numbers", mobileNo);
                body.put("message", "40955");
                body.put("variables", "{#FF#}");
                body.put("variables_values", order_id);

                return body;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(DeliveryActivity.this);
        requestQueue.add(stringRequest);
        /////sent confirmation sms

        if (fromCart) {
            loadingDialog.show();
            Map<String, Object> updateCartList = new HashMap<>();
            long cartListSize = 0;
            final List<Integer> indexList = new ArrayList<>();

            for (int x = 0; x < DBQueries.cartList.size(); x++) {
                if (!cartItemModelList.get(x).isInStock()) {
                    updateCartList.put("product_ID_" + cartListSize, cartItemModelList.get(x).getProductID());
                    cartListSize++;
                } else {
                    indexList.add(x);
                }
            }
            updateCartList.put("list_size", cartListSize);
            FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA")
                    .document("MY_CART").set(updateCartList).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        for (int x = 0; x < indexList.size(); x++) {
                            DBQueries.cartList.remove(indexList.get(x).intValue());
                            DBQueries.cartItemModelList.remove(indexList.get(x).intValue());
                            DBQueries.cartItemModelList.remove(DBQueries.cartItemModelList.size() - 1);
                        }

                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                    loadingDialog.dismiss();
                }
            });
        }

        continueBtn.setEnabled(false);
        changeAddressBtn.setEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        orderId.setText("Order ID " + order_id);
        orderConfirmationLayout.setVisibility(View.VISIBLE);
        continueShoppingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeliveryActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void placeOrderDetails() {

        String userId = FirebaseAuth.getInstance().getUid();
        loadingDialog.show();
        for (CartItemModel cartItemModel : cartItemModelList) {
            if (cartItemModel.getType() == CartItemModel.CART_ITEM) {
                Map<String, Object> orderDetails = new HashMap<>();
                orderDetails.put("ORDER ID", order_id);
                orderDetails.put("Product Id", cartItemModel.getProductID());
                orderDetails.put("Product Image", cartItemModel.getProductImage());
                orderDetails.put("Product Title", cartItemModel.getProductTitle());
                orderDetails.put("User Id", userId);
                orderDetails.put("Product Quantity", cartItemModel.getProductQuantity());
                if (cartItemModel.getCuttedPrice() != null) {
                    orderDetails.put("Cutted Price", cartItemModel.getCuttedPrice());
                } else {
                    orderDetails.put("Cutted Price", "");
                }
                orderDetails.put("Product Price", cartItemModel.getProductPrice());
                if (cartItemModel.getSelectedCouponId() != null) {
                    orderDetails.put("Coupon Id", cartItemModel.getSelectedCouponId());
                } else {
                    orderDetails.put("Coupon Id", "");
                }
                if (cartItemModel.getDiscountedPrice() != null) {
                    orderDetails.put("Discounted Price", cartItemModel.getDiscountedPrice());
                } else {
                    orderDetails.put("Discounted Price", "");
                }
                orderDetails.put("Ordered Date", FieldValue.serverTimestamp());
                orderDetails.put("Packed Date", FieldValue.serverTimestamp());
                orderDetails.put("Shipped Date", FieldValue.serverTimestamp());
                orderDetails.put("Delivered Date", FieldValue.serverTimestamp());
                orderDetails.put("Cancelled Date", FieldValue.serverTimestamp());
                orderDetails.put("Order Status", "Ordered");
                orderDetails.put("Payment Method", paymentMethod);
                orderDetails.put("Address", fullAddress.getText());
                orderDetails.put("Full Name", fullName.getText());
                orderDetails.put("Pincode", pinCode.getText());
                orderDetails.put("Free Coupons", cartItemModel.getFreeCoupons());
                orderDetails.put("Delivery Price", cartItemModelList.get(cartItemModelList.size() - 1).getDeliveryPrice());
                orderDetails.put("Cancellation requested", false);

                firebaseFirestore.collection("ORDERS").document(order_id).collection("OrderItems").document(cartItemModel.getProductID())
                        .set(orderDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            String error = task.getException().getMessage();
                            Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Map<String, Object> orderDetails = new HashMap<>();
                orderDetails.put("Total Items", cartItemModel.getTotalItems());
                orderDetails.put("Total Items Price", cartItemModel.getTotalItemsPrice());
                orderDetails.put("Delivery Price", cartItemModel.getDeliveryPrice());
                orderDetails.put("Total Amount", cartItemModel.getTotalAmount());
                orderDetails.put("Saved Amount", cartItemModel.getSavedAmount());
                orderDetails.put("Payment Status", "Not Paid");
                orderDetails.put("Order Status", "Cancelled");

                firebaseFirestore.collection("ORDERS").document(order_id)
                        .set(orderDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (paymentMethod.equals("PAYTM")) {
                                paytm();
                            } else {
                                cod();
                            }

                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    private void paytm() {
        getQtyIDs = false;
        paymentMethodDialog.dismiss();
        loadingDialog.show();

        Checkout checkout = new Checkout();

        checkout.setImage(R.mipmap.app_icon);

        final Activity activity = this;

        try {
            JSONObject options = new JSONObject();

            options.put("name", "My Mall");
            //options.put("order_id", order_id);//from response of step 3.
            options.put("currency", "INR");
            options.put("amount", (Integer.parseInt(totalAmount.getText().toString().substring(3, totalAmount.getText().length() - 2)) * 100));//pass amount in currency subunits
            checkout.open(activity, options);
        } catch (Exception e) {
            Log.e("TAG", "Error in starting Razorpay Checkout", e);
        }
    }

    private void cod() {
        getQtyIDs = false;
        paymentMethodDialog.dismiss();
        Intent otpIntent = new Intent(DeliveryActivity.this, OTPVerificationActivity.class);
        otpIntent.putExtra("OTP", mobileNo.substring(0, 10));
        otpIntent.putExtra("order", order_id);
        startActivity(otpIntent);
    }
}