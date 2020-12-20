package rishabh.example.mymall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class OrderDetailsActivity extends AppCompatActivity {

    private int position;

    private TextView title, price, quantity;
    private ImageView productImage, orderedIndicator, packedIndicator, shippedIndicator, deliveredIndicator;
    private ProgressBar O_P_progress, P_S_progress, S_D_progress;
    private TextView orderedTitle, packedTitle, shippedTitle, deliveredTitle;
    private TextView orderedDate, packedDate, shippedDate, deliveredDate;
    private TextView orderedBody, packedBody, shippedBody, deliveredBody;
    private LinearLayout rateNowContainer;
    private int rating;
    private TextView fullName, address, pinCode;
    private TextView totalItemsPrice, deliveryPrice, totalAmount, totalItems, savedAmount;
    private Dialog loadingDialog, cancelDialog;
    private SimpleDateFormat simpleDateFormat;
    private Button cancelOrderBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Order Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ////loading dialog
        loadingDialog = new Dialog(OrderDetailsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ////loading dialog

        ////cancellation dialog
        cancelDialog = new Dialog(OrderDetailsActivity.this);
        cancelDialog.setContentView(R.layout.order_cancel_dialog);
        cancelDialog.setCancelable(false);
        cancelDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        //cancelDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ////cancellation dialog

        position = getIntent().getIntExtra("position", -1);
        final MyOrderItemModel model = DBQueries.myOrderItemModelList.get(position);

        title = findViewById(R.id.product_title_order_details);
        price = findViewById(R.id.product_price_order_details);
        quantity = findViewById(R.id.product_quantity_order_details);
        productImage = findViewById(R.id.product_image_order_details);
        orderedIndicator = findViewById(R.id.ordered_indicator);
        packedIndicator = findViewById(R.id.packed_indicator);
        shippedIndicator = findViewById(R.id.shipping_indicator);
        deliveredIndicator = findViewById(R.id.delivered_indicator);
        O_P_progress = findViewById(R.id.ordered_packed_progress);
        P_S_progress = findViewById(R.id.packed_shipping_progress);
        S_D_progress = findViewById(R.id.shipping_delivered_progress);
        orderedTitle = findViewById(R.id.ordered_title);
        packedTitle = findViewById(R.id.packed_title);
        shippedTitle = findViewById(R.id.shipping_title);
        deliveredTitle = findViewById(R.id.delivered_title);
        orderedDate = findViewById(R.id.ordered_date);
        packedDate = findViewById(R.id.packed_date);
        shippedDate = findViewById(R.id.shipping_date);
        deliveredDate = findViewById(R.id.delivered_date);
        orderedBody = findViewById(R.id.ordered_body);
        packedBody = findViewById(R.id.packed_body);
        shippedBody = findViewById(R.id.shipping_body);
        deliveredBody = findViewById(R.id.delivered_body);
        rateNowContainer = findViewById(R.id.rate_now_container);
        fullName = findViewById(R.id.fullname);
        address = findViewById(R.id.address);
        pinCode = findViewById(R.id.pincode);
        totalItemsPrice = findViewById(R.id.total_items_price);
        deliveryPrice = findViewById(R.id.delivery_price);
        totalAmount = findViewById(R.id.total_price);
        totalItems = findViewById(R.id.total_items);
        savedAmount = findViewById(R.id.saved_amount);
        cancelOrderBtn = findViewById(R.id.cancel_btn);

        title.setText(model.getProductTitle());
        if (!model.getDiscountedPrice().equals("")) {
            price.setText("Rs." + model.getDiscountedPrice() + "/-");
        } else {
            price.setText("Rs." + model.getProductPrice() + "/-");
        }
        quantity.setText("Qty: " + String.valueOf(model.getProductQuantity()));
        Glide.with(this).load(model.getProductImage()).into(productImage);

        simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy - hh:mm aa");
        switch (model.getOrderStatus()) {
            case "Ordered":
                orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                orderedDate.setText(String.valueOf(simpleDateFormat.format(model.getOrderedDate())));

                O_P_progress.setVisibility(View.GONE);
                P_S_progress.setVisibility(View.GONE);
                S_D_progress.setVisibility(View.GONE);

                packedIndicator.setVisibility(View.GONE);
                packedBody.setVisibility(View.GONE);
                packedDate.setVisibility(View.GONE);
                packedTitle.setVisibility(View.GONE);

                shippedIndicator.setVisibility(View.GONE);
                shippedBody.setVisibility(View.GONE);
                shippedDate.setVisibility(View.GONE);
                shippedTitle.setVisibility(View.GONE);

                deliveredIndicator.setVisibility(View.GONE);
                deliveredBody.setVisibility(View.GONE);
                deliveredDate.setVisibility(View.GONE);
                deliveredTitle.setVisibility(View.GONE);
                break;

            case "Packed":
                orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                orderedDate.setText(String.valueOf(simpleDateFormat.format(model.getOrderedDate())));

                packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                packedDate.setText(String.valueOf(simpleDateFormat.format(model.getPackedDate())));

                O_P_progress.setProgress(100, true);

                P_S_progress.setVisibility(View.GONE);
                S_D_progress.setVisibility(View.GONE);

                shippedIndicator.setVisibility(View.GONE);
                shippedBody.setVisibility(View.GONE);
                shippedDate.setVisibility(View.GONE);
                shippedTitle.setVisibility(View.GONE);

                deliveredIndicator.setVisibility(View.GONE);
                deliveredBody.setVisibility(View.GONE);
                deliveredDate.setVisibility(View.GONE);
                deliveredTitle.setVisibility(View.GONE);

                break;

            case "Shipped":
                orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                orderedDate.setText(String.valueOf(simpleDateFormat.format(model.getOrderedDate())));

                packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                packedDate.setText(String.valueOf(simpleDateFormat.format(model.getPackedDate())));

                shippedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                shippedDate.setText(String.valueOf(simpleDateFormat.format(model.getShippedDate())));

                O_P_progress.setProgress(100, true);
                P_S_progress.setProgress(100, true);

                S_D_progress.setVisibility(View.GONE);

                deliveredIndicator.setVisibility(View.GONE);
                deliveredBody.setVisibility(View.GONE);
                deliveredDate.setVisibility(View.GONE);
                deliveredTitle.setVisibility(View.GONE);

                break;

            case "Out for Delivery":
                orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                orderedDate.setText(String.valueOf(simpleDateFormat.format(model.getOrderedDate())));

                packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                packedDate.setText(String.valueOf(simpleDateFormat.format(model.getPackedDate())));

                shippedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                shippedDate.setText(String.valueOf(simpleDateFormat.format(model.getShippedDate())));

                deliveredIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                deliveredDate.setText(String.valueOf(simpleDateFormat.format(model.getDeliveredDate())));

                O_P_progress.setProgress(100, true);
                P_S_progress.setProgress(100, true);
                S_D_progress.setProgress(100, true);

                deliveredTitle.setText("Out for Delivery");
                deliveredBody.setText("Your order is out for delivery.");

                break;

            case "Delivered":
                orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                orderedDate.setText(String.valueOf(simpleDateFormat.format(model.getOrderedDate())));

                packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                packedDate.setText(String.valueOf(simpleDateFormat.format(model.getPackedDate())));

                shippedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                shippedDate.setText(String.valueOf(simpleDateFormat.format(model.getShippedDate())));

                deliveredIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                deliveredDate.setText(String.valueOf(simpleDateFormat.format(model.getDeliveredDate())));

                O_P_progress.setProgress(100, true);
                P_S_progress.setProgress(100, true);
                S_D_progress.setProgress(100, true);

                break;

            case "Cancelled":

                if (model.getPackedDate().after(model.getOrderedDate())) {

                    if (model.getShippedDate().after(model.getPackedDate())) {

                        orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                        orderedDate.setText(String.valueOf(simpleDateFormat.format(model.getOrderedDate())));

                        packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                        packedDate.setText(String.valueOf(simpleDateFormat.format(model.getPackedDate())));

                        shippedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                        shippedDate.setText(String.valueOf(simpleDateFormat.format(model.getShippedDate())));

                        deliveredIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                        deliveredDate.setText(String.valueOf(simpleDateFormat.format(model.getCancelledDate())));

                        deliveredTitle.setText("Cancelled");
                        deliveredBody.setText("Your order has been cancelled.");

                        O_P_progress.setProgress(100, true);
                        P_S_progress.setProgress(100, true);
                        S_D_progress.setProgress(100, true);

                    } else {
                        orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                        orderedDate.setText(String.valueOf(simpleDateFormat.format(model.getOrderedDate())));

                        packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                        packedDate.setText(String.valueOf(simpleDateFormat.format(model.getPackedDate())));

                        shippedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                        shippedDate.setText(String.valueOf(simpleDateFormat.format(model.getCancelledDate())));

                        shippedTitle.setText("Cancelled");
                        shippedBody.setText("Your order has been cancelled.");

                        O_P_progress.setProgress(100, true);
                        P_S_progress.setProgress(100, true);

                        S_D_progress.setVisibility(View.GONE);

                        deliveredIndicator.setVisibility(View.GONE);
                        deliveredBody.setVisibility(View.GONE);
                        deliveredDate.setVisibility(View.GONE);
                        deliveredTitle.setVisibility(View.GONE);
                    }

                } else {
                    orderedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
                    orderedDate.setText(String.valueOf(simpleDateFormat.format(model.getOrderedDate())));

                    packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                    packedDate.setText(String.valueOf(simpleDateFormat.format(model.getCancelledDate())));

                    packedTitle.setText("Cancelled");
                    packedBody.setText("Your order has been cancelled.");

                    O_P_progress.setProgress(100, true);

                    P_S_progress.setVisibility(View.GONE);
                    S_D_progress.setVisibility(View.GONE);

                    shippedIndicator.setVisibility(View.GONE);
                    shippedBody.setVisibility(View.GONE);
                    shippedDate.setVisibility(View.GONE);
                    shippedTitle.setVisibility(View.GONE);

                    deliveredIndicator.setVisibility(View.GONE);
                    deliveredBody.setVisibility(View.GONE);
                    deliveredDate.setVisibility(View.GONE);
                    deliveredTitle.setVisibility(View.GONE);
                }

                break;
        }

        /////rating layout
        rating = model.getRating();

        setRating(rating);

        for (int x = 0; x < rateNowContainer.getChildCount(); x++) {
            final int starPosition = x;
            rateNowContainer.getChildAt(x).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadingDialog.show();
                    setRating(starPosition);
                    final DocumentReference documentReference = FirebaseFirestore.getInstance().collection("PRODUCTS").document(model.getProductId());

                    FirebaseFirestore.getInstance().runTransaction(new Transaction.Function<Object>() {
                        @Nullable
                        @Override
                        public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                            DocumentSnapshot documentSnapshot = transaction.get(documentReference);

                            if (rating != 0) {
                                Long increase = documentSnapshot.getLong(starPosition + 1 + "_star") + 1;
                                Long decrease = documentSnapshot.getLong(rating + 1 + "_star") - 1;
                                transaction.update(documentReference, starPosition + 1 + "_star", increase);
                                transaction.update(documentReference, rating + 1 + "_star", decrease);
                            } else {
                                Long increase = documentSnapshot.getLong(starPosition + 1 + "_star") + 1;
                                transaction.update(documentReference, starPosition + 1 + "_star", increase);
                            }
                            return null;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Object>() {
                        @Override
                        public void onSuccess(Object object) {
                            Map<String, Object> myRating = new HashMap<>();
                            if (DBQueries.myRatedIds.contains(model.getProductId())) {
                                myRating.put("rating_" + DBQueries.myRatedIds.indexOf(model.getProductId()), (long) starPosition + 1);
                            } else {
                                myRating.put("list_size", (long) DBQueries.myRatedIds.size() + 1);
                                myRating.put("product_ID_" + DBQueries.myRatedIds.size(), model.getProductId());
                                myRating.put("rating_" + DBQueries.myRatedIds.size(), (long) starPosition + 1);
                            }

                            FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_RATINGS")
                                    .update(myRating).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        DBQueries.myOrderItemModelList.get(position).setRating(starPosition);
                                        if (DBQueries.myRatedIds.contains(model.getProductId())) {
                                            DBQueries.myRating.set(DBQueries.myRatedIds.indexOf(model.getProductId()), Long.parseLong(String.valueOf(starPosition + 1)));
                                        } else {
                                            DBQueries.myRatedIds.add(model.getProductId());
                                            DBQueries.myRating.add(Long.parseLong(String.valueOf(starPosition + 1)));
                                        }
                                    } else {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(OrderDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                                    }
                                    loadingDialog.dismiss();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loadingDialog.dismiss();
                        }
                    });
                }
            });
        }

        /////rating layout

        if (model.isCancellationRequested()) {
            cancelOrderBtn.setVisibility(View.VISIBLE);
            cancelOrderBtn.setEnabled(false);
            cancelOrderBtn.setText("Cancellation in Process");
            cancelOrderBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
            cancelOrderBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ffffff")));
        } else {
            if (model.getOrderStatus().equals("Ordered") || model.getOrderStatus().equals("Packed")) {
                cancelOrderBtn.setVisibility(View.VISIBLE);
                cancelOrderBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancelDialog.findViewById(R.id.no_btn).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelDialog.dismiss();
                            }
                        });

                        cancelDialog.findViewById(R.id.yes_btn).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelDialog.dismiss();
                                loadingDialog.show();
                                Map<String, Object> map = new HashMap<>();
                                map.put("ORDER ID", model.getOrderId());
                                map.put("Product Id", model.getProductId());
                                map.put("Order Cancelled", false);
                                FirebaseFirestore.getInstance().collection("CANCELLED ORDERS").document().set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseFirestore.getInstance().collection("ORDERS").document(model.getOrderId()).collection("OrderItems").document(model.getProductId()).update("Cancellation requested", true)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                model.setCancellationRequested(true);
                                                                cancelOrderBtn.setEnabled(false);
                                                                cancelOrderBtn.setText("Cancellation in Process");
                                                                cancelOrderBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
                                                                cancelOrderBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ffffff")));

                                                            }
                                                            else {
                                                                String error = task.getException().getMessage();
                                                                Toast.makeText(OrderDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                                                            }
                                                            loadingDialog.dismiss();
                                                        }
                                                    });
                                        } else {
                                            loadingDialog.dismiss();
                                            String error = task.getException().getMessage();
                                            Toast.makeText(OrderDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                        cancelDialog.show();
                    }
                });
            }
        }

        fullName.setText(model.getFullName());
        address.setText(model.getAddress());
        pinCode.setText(model.getPinCode());

        totalItems.setText("Price (" + model.getProductQuantity() + " items)");

        Long totalItemsPriceValue;
        if (model.getDiscountedPrice().equals("")) {
            totalItemsPriceValue = model.getProductQuantity() * Long.valueOf(model.getProductPrice());
            totalItemsPrice.setText("Rs." + totalItemsPriceValue + "/-");
        } else {
            totalItemsPriceValue = model.getProductQuantity() * Long.valueOf(model.getDiscountedPrice());
            totalItemsPrice.setText("Rs." + totalItemsPriceValue + "/-");
        }
        if (model.getDeliveryPrice().equals("FREE")) {
            deliveryPrice.setText(model.getDeliveryPrice());
            totalAmount.setText(totalItemsPrice.getText());
        } else {
            deliveryPrice.setText("Rs." + model.getDeliveryPrice() + "/-");
            totalAmount.setText("Rs." + (totalItemsPriceValue + Long.valueOf(model.getDeliveryPrice())) + "/-");
        }
        if (!model.getCuttedPrice().equals("")) {
            if (!model.getDiscountedPrice().equals("")) {
                savedAmount.setText("You saved Rs." + model.getProductQuantity() * (Long.valueOf(model.getCuttedPrice()) - Long.valueOf(model.getDiscountedPrice())) + "/- on this order");
            } else {
                savedAmount.setText("You saved Rs." + model.getProductQuantity() * (Long.valueOf(model.getCuttedPrice()) - Long.valueOf(model.getProductPrice())) + "/- on this order");
            }
        } else {
            if (!model.getDiscountedPrice().equals("")) {
                savedAmount.setText("You saved Rs." + model.getProductQuantity() * (Long.valueOf(model.getProductPrice()) - Long.valueOf(model.getDiscountedPrice())) + "/- on this order");
            } else {
                savedAmount.setText("You saved Rs.0/- on this order");
            }
        }

    }

    private void setRating(int starPosition) {
        for (int x = 0; x < rateNowContainer.getChildCount(); x++) {
            ImageView starBtn = (ImageView) rateNowContainer.getChildAt(x);
            starBtn.setImageTintList(ColorStateList.valueOf(Color.parseColor("#bebebe")));
            if (x <= starPosition) {
                starBtn.setImageTintList(ColorStateList.valueOf(Color.parseColor("#ffbb00")));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}