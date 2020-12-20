package rishabh.example.mymall;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CartAdapter extends RecyclerView.Adapter {

    private List<CartItemModel> cartItemModelList;
    private int lastPosition = -1;
    private TextView cartTotalAmount;
    private boolean showDeleteBtn;

    public CartAdapter(List<CartItemModel> cartItemModelList, TextView cartTotalAmount, boolean showDeleteBtn) {
        this.cartItemModelList = cartItemModelList;
        this.cartTotalAmount = cartTotalAmount;
        this.showDeleteBtn = showDeleteBtn;
    }

    @Override
    public int getItemViewType(int position) {
        switch (cartItemModelList.get(position).getType()) {
            case 0:
                return CartItemModel.CART_ITEM;

            case 1:
                return CartItemModel.TOTAL_AMOUNT;

            default:
                return -1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        switch (viewType) {
            case CartItemModel.CART_ITEM:
                View cartItemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cart_item_layout, viewGroup, false);
                return new CartItemViewholder(cartItemView);

            case CartItemModel.TOTAL_AMOUNT:
                View cartTotalView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cart_total_amount_layout, viewGroup, false);
                return new CartTotalAmountViewholder(cartTotalView);

            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        switch (cartItemModelList.get(position).getType()) {
            case CartItemModel.CART_ITEM:
                String productID = cartItemModelList.get(position).getProductID();
                String resource = cartItemModelList.get(position).getProductImage();
                String title = cartItemModelList.get(position).getProductTitle();
                Long freeCoupons = cartItemModelList.get(position).getFreeCoupons();
                String productPrice = cartItemModelList.get(position).getProductPrice();
                String cuttedPrice = cartItemModelList.get(position).getCuttedPrice();
                Long offersApplied = cartItemModelList.get(position).getOfferApplied();
                boolean inStock = cartItemModelList.get(position).isInStock();
                Long productQuantity = cartItemModelList.get(position).getProductQuantity();
                Long maxQuantity = cartItemModelList.get(position).getMaxQuantity();
                boolean qtyError = cartItemModelList.get(position).isQtyError();
                List<String> qtyIds = cartItemModelList.get(position).getQtyIDs();
                long stockQty = cartItemModelList.get(position).getStockQuantity();
                boolean COD = cartItemModelList.get(position).isCOD();

                ((CartItemViewholder) viewHolder).setItemDetails(productID, resource, title, freeCoupons, productPrice, cuttedPrice, offersApplied, position, inStock, String.valueOf(productQuantity), maxQuantity, qtyError, qtyIds, stockQty, COD);
                break;

            case CartItemModel.TOTAL_AMOUNT:
                int totalItems = 0;
                int totalItemPrice = 0;
                String deliveryPrice;
                int totalAmount;
                int savedAmount = 0;

                for (int x = 0; x < cartItemModelList.size(); x++) {
                    if (cartItemModelList.get(x).getType() == CartItemModel.CART_ITEM && cartItemModelList.get(x).isInStock()) {
                        int quantity = Integer.parseInt(String.valueOf(cartItemModelList.get(x).getProductQuantity()));
                        totalItems = totalItems + quantity;
                        if (TextUtils.isEmpty(cartItemModelList.get(x).getSelectedCouponId())) {
                            totalItemPrice = totalItemPrice + Integer.parseInt(cartItemModelList.get(x).getProductPrice()) * quantity;
                        } else {
                            totalItemPrice = totalItemPrice + Integer.parseInt(cartItemModelList.get(x).getDiscountedPrice()) * quantity;
                        }

                        if (!TextUtils.isEmpty(cartItemModelList.get(x).getCuttedPrice())) {
                            savedAmount = savedAmount + (Integer.parseInt(cartItemModelList.get(x).getCuttedPrice()) - Integer.parseInt(cartItemModelList.get(x).getProductPrice())) * quantity;
                            if (!TextUtils.isEmpty(cartItemModelList.get(x).getSelectedCouponId())) {
                                savedAmount = savedAmount + (Integer.parseInt(cartItemModelList.get(x).getProductPrice()) - Integer.parseInt(cartItemModelList.get(x).getDiscountedPrice())) * quantity;
                            }
                        } else {
                            if (!TextUtils.isEmpty(cartItemModelList.get(x).getSelectedCouponId())) {
                                savedAmount = savedAmount + (Integer.parseInt(cartItemModelList.get(x).getProductPrice()) - Integer.parseInt(cartItemModelList.get(x).getDiscountedPrice())) * quantity;
                            }
                        }
                    }
                }
                if (totalItemPrice > 500) {
                    deliveryPrice = "FREE";
                    totalAmount = totalItemPrice;
                } else {
                    deliveryPrice = "60";
                    totalAmount = totalItemPrice + 60;
                }
                cartItemModelList.get(position).setTotalItems(totalItems);
                cartItemModelList.get(position).setTotalItemsPrice(totalItemPrice);
                cartItemModelList.get(position).setDeliveryPrice(deliveryPrice);
                cartItemModelList.get(position).setTotalAmount(totalAmount);
                cartItemModelList.get(position).setSavedAmount(savedAmount);
                ((CartTotalAmountViewholder) viewHolder).setTotalAmount(totalItems, totalItemPrice, deliveryPrice, totalAmount, savedAmount);
                break;

            default:
                return;
        }

        if (lastPosition < position) {
            Animation animation = AnimationUtils.loadAnimation(viewHolder.itemView.getContext(), R.anim.fade_in);
            viewHolder.itemView.setAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return cartItemModelList.size();
    }

    class CartItemViewholder extends RecyclerView.ViewHolder {

        private ImageView productImage, freeCouponIcon;
        private TextView productTitle, freeCoupons, productPrice, cuttedPrice, offersApplied, couponsApplied, productQuantity, couponRedemptionBody;
        private LinearLayout deleteBtn, couponRedemptionLayout;
        private Button redeemBtn;
        private ImageView codIndicator;

        /////couponDialog

        private TextView couponTitle, couponBody, couponExpiryDate, discountedPrice, originalPrice, footerText;
        private RecyclerView couponsRecyclerView;
        private LinearLayout selectedCoupon, applyorRemoveBtnContainer;
        private Button removeCouponBtn, applyCouponBtn;
        private String productOriginalPrice;

        /////couponDialog

        public CartItemViewholder(@NonNull View itemView) {
            super(itemView);

            productImage = itemView.findViewById(R.id.product_image);
            freeCouponIcon = itemView.findViewById(R.id.free_coupon_icon);
            productTitle = itemView.findViewById(R.id.product_title_cart);
            freeCoupons = itemView.findViewById(R.id.tv_free_coupon);
            productPrice = itemView.findViewById(R.id.product_price_cart);
            cuttedPrice = itemView.findViewById(R.id.cutted_price_cart);
            offersApplied = itemView.findViewById(R.id.offer_applied);
            couponsApplied = itemView.findViewById(R.id.coupons_applied);
            productQuantity = itemView.findViewById(R.id.product_quantity);
            redeemBtn = itemView.findViewById(R.id.coupon_redemption_btn);
            deleteBtn = itemView.findViewById(R.id.remove_item_btn);
            couponRedemptionLayout = itemView.findViewById(R.id.coupan_reedemtion_layout);
            couponRedemptionBody = itemView.findViewById(R.id.tv_coupon_redemption);
            codIndicator = itemView.findViewById(R.id.cod_indicator);
        }

        private void setItemDetails(final String productID, String resource, String title, Long freeCouponsNo, final String productPriceText, String cuttedPriceText, Long offersAppliedNo, final int position, boolean inStock, final String quantity, final Long maxQuantity, boolean qtyError, final List<String> qtyIds, final long stockQty, boolean COD) {
            Glide.with(itemView.getContext()).load(resource).apply(new RequestOptions().placeholder(R.mipmap.icon_place_holder)).into(productImage);
            productTitle.setText(title);

            final Dialog checkCouponPriceDialog = new Dialog(itemView.getContext());
            checkCouponPriceDialog.setContentView(R.layout.coupon_redeem_dialog);
            checkCouponPriceDialog.setCancelable(false);
            checkCouponPriceDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            if (COD){
                codIndicator.setVisibility(View.VISIBLE);
            }
            else {
                codIndicator.setVisibility(View.INVISIBLE);
            }

            if (inStock) {
                if (freeCouponsNo > 0) {
                    freeCouponIcon.setVisibility(View.VISIBLE);
                    freeCoupons.setVisibility(View.VISIBLE);
                    if (freeCouponsNo == 1) {
                        freeCoupons.setText("Free " + freeCouponsNo + " Coupon");
                    } else {
                        freeCoupons.setText("Free " + freeCouponsNo + " Coupons");
                    }
                } else {
                    freeCouponIcon.setVisibility(View.INVISIBLE);
                    freeCoupons.setVisibility(View.INVISIBLE);
                }

                productPrice.setText("Rs." + productPriceText + "/-");
                productPrice.setTextColor(Color.parseColor("#000000"));
                cuttedPrice.setText("Rs." + cuttedPriceText + "/-");
                couponRedemptionLayout.setVisibility(View.VISIBLE);

                /////coupon Dialog

                ImageView toggleRecyclerView = checkCouponPriceDialog.findViewById(R.id.toggle_recyclerview);
                couponsRecyclerView = checkCouponPriceDialog.findViewById(R.id.coupons_recyclerview);
                selectedCoupon = checkCouponPriceDialog.findViewById(R.id.selected_coupon);
                couponTitle = checkCouponPriceDialog.findViewById(R.id.coupon_title);
                couponBody = checkCouponPriceDialog.findViewById(R.id.coupon_body);
                couponExpiryDate = checkCouponPriceDialog.findViewById(R.id.coupon_validity);
                removeCouponBtn = checkCouponPriceDialog.findViewById(R.id.remove_btn);
                applyCouponBtn = checkCouponPriceDialog.findViewById(R.id.apply_btn);
                footerText = checkCouponPriceDialog.findViewById(R.id.footer_text);
                applyorRemoveBtnContainer = checkCouponPriceDialog.findViewById(R.id.apply_or_remove_btns_container);

                footerText.setVisibility(View.GONE);
                applyorRemoveBtnContainer.setVisibility(View.VISIBLE);
                originalPrice = checkCouponPriceDialog.findViewById(R.id.original_price);
                discountedPrice = checkCouponPriceDialog.findViewById(R.id.discounted_price);

                LinearLayoutManager layoutManager = new LinearLayoutManager(itemView.getContext());
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                couponsRecyclerView.setLayoutManager(layoutManager);

                originalPrice.setText(productPrice.getText());
                productOriginalPrice = productPriceText;
                MyRewardsAdapter myRewardsAdapter = new MyRewardsAdapter(position, DBQueries.rewardsModelList, true, couponsRecyclerView, selectedCoupon, productOriginalPrice, couponTitle, couponExpiryDate, couponBody, discountedPrice, cartItemModelList);
                couponsRecyclerView.setAdapter(myRewardsAdapter);
                myRewardsAdapter.notifyDataSetChanged();

                applyCouponBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!TextUtils.isEmpty(cartItemModelList.get(position).getSelectedCouponId())) {
                            for (RewardsModel rewardsModel : DBQueries.rewardsModelList) {
                                if (rewardsModel.getCouponId().equals(cartItemModelList.get(position).getSelectedCouponId())) {
                                    rewardsModel.setAlreadyUsed(true);
                                    couponRedemptionLayout.setBackground(itemView.getContext().getResources().getDrawable(R.drawable.reward_gradient_background));
                                    couponRedemptionBody.setText(rewardsModel.getCouponBody());
                                    redeemBtn.setText("Coupon");
                                }
                            }
                            couponsApplied.setVisibility(View.VISIBLE);
                            cartItemModelList.get(position).setDiscountedPrice(discountedPrice.getText().toString().substring(3, discountedPrice.getText().length() - 2));
                            productPrice.setText(discountedPrice.getText());
                            String offerDiscountedAmount = String.valueOf(Long.valueOf(productPriceText) - Long.valueOf(discountedPrice.getText().toString().substring(3, discountedPrice.getText().length() - 2)));
                            couponsApplied.setText("Coupon applied - Rs." + offerDiscountedAmount + "/-");
                            notifyItemChanged(cartItemModelList.size() - 1);
                            checkCouponPriceDialog.dismiss();
                        }
                    }
                });

                removeCouponBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (RewardsModel rewardsModel : DBQueries.rewardsModelList) {
                            if (rewardsModel.getCouponId().equals(cartItemModelList.get(position).getSelectedCouponId())) {
                                rewardsModel.setAlreadyUsed(false);
                            }
                        }
                        couponTitle.setText("Coupon");
                        couponExpiryDate.setText("Validity");
                        couponBody.setText("Tap the icon on top right corner to select your coupon.");
                        couponsApplied.setVisibility(View.INVISIBLE);
                        couponRedemptionLayout.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.couponRed));
                        couponRedemptionBody.setText("Apply your coupon here.");
                        redeemBtn.setText("Redeem");
                        discountedPrice.setText("");
                        cartItemModelList.get(position).setSelectedCouponId(null);
                        productPrice.setText("Rs." + productPriceText + "/-");
                        notifyItemChanged(cartItemModelList.size() - 1);
                        checkCouponPriceDialog.dismiss();
                    }
                });

                toggleRecyclerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialogRecyclerView();
                    }
                });

                if (!TextUtils.isEmpty(cartItemModelList.get(position).getSelectedCouponId())) {
                    for (RewardsModel rewardsModel : DBQueries.rewardsModelList) {
                        if (rewardsModel.getCouponId().equals(cartItemModelList.get(position).getSelectedCouponId())) {
                            couponRedemptionLayout.setBackground(itemView.getContext().getResources().getDrawable(R.drawable.reward_gradient_background));
                            couponRedemptionBody.setText(rewardsModel.getCouponBody());
                            redeemBtn.setText("Coupon");

                            couponBody.setText(rewardsModel.getCouponBody());
                            if (rewardsModel.getType().equals("Discount")) {
                                couponTitle.setText(rewardsModel.getType());
                            } else {
                                couponTitle.setText("FLAT Rs." + rewardsModel.getDiscORamt() + "/- OFF");
                            }
                            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy");
                            couponExpiryDate.setText("till " + simpleDateFormat.format(rewardsModel.getTimestamp()));
                        }
                    }
                    discountedPrice.setText("Rs." + cartItemModelList.get(position).getDiscountedPrice() + "/-");
                    couponsApplied.setVisibility(View.VISIBLE);
                    productPrice.setText("Rs." + cartItemModelList.get(position).getDiscountedPrice() + "/-");
                    String offerDiscountedAmount = String.valueOf(Long.valueOf(productPriceText) - Long.valueOf(cartItemModelList.get(position).getDiscountedPrice()));
                    couponsApplied.setText("Coupon applied - Rs." + offerDiscountedAmount + "/-");
                } else {
                    couponsApplied.setVisibility(View.INVISIBLE);
                    couponRedemptionLayout.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.couponRed));
                    couponRedemptionBody.setText("Apply your coupon here.");
                    redeemBtn.setText("Redeem");
                }
                /////coupon Dialog

                productQuantity.setText("Qty: " + quantity);
                if (!showDeleteBtn) {
                    if (qtyError) {
                        productQuantity.setTextColor(itemView.getContext().getResources().getColor(R.color.colorPrimary));
                        productQuantity.setBackgroundTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.colorPrimary)));
                        productQuantity.setCompoundDrawableTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.colorPrimary)));
                    } else {
                        productQuantity.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
                        productQuantity.setBackgroundTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.black)));
                        productQuantity.setCompoundDrawableTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.black)));
                    }
                }

                productQuantity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Dialog quantityDialog = new Dialog(itemView.getContext());
                        quantityDialog.setContentView(R.layout.quantity_dialog);
                        quantityDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        quantityDialog.setCancelable(false);
                        final EditText quantityNo = quantityDialog.findViewById(R.id.quantity_number);
                        Button cancelBtn = quantityDialog.findViewById(R.id.cancel_dialog_btn);
                        Button okBtn = quantityDialog.findViewById(R.id.ok_dialog_btn);
                        quantityNo.setHint("Max " + String.valueOf(maxQuantity));

                        cancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                quantityDialog.dismiss();
                            }
                        });

                        okBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!TextUtils.isEmpty(quantityNo.getText())) {
                                    if (Long.valueOf(quantityNo.getText().toString()) <= maxQuantity && Long.valueOf(quantityNo.getText().toString()) != 0) {
                                        if (itemView.getContext() instanceof MainActivity) {
                                            cartItemModelList.get(position).setProductQuantity(Long.valueOf(quantityNo.getText().toString()));
                                        } else {
                                            if (DeliveryActivity.fromCart) {
                                                cartItemModelList.get(position).setProductQuantity(Long.valueOf(quantityNo.getText().toString()));
                                            } else {
                                                DeliveryActivity.cartItemModelList.get(position).setProductQuantity(Long.valueOf(quantityNo.getText().toString()));
                                            }
                                        }
                                        productQuantity.setText("Qty: " + quantityNo.getText());
                                        notifyItemChanged(cartItemModelList.size() - 1);

                                        if (!showDeleteBtn) {
                                            DeliveryActivity.loadingDialog.show();
                                            DeliveryActivity.cartItemModelList.get(position).setQtyError(false);
                                            final int initialQty = Integer.parseInt(quantity);
                                            final int finalQty = Integer.parseInt(quantityNo.getText().toString());
                                            final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

                                            if (finalQty > initialQty) {

                                                for (int y = 0; y < finalQty - initialQty; y++) {
                                                    final String quantityDocumentName = UUID.randomUUID().toString().substring(0, 20);

                                                    Map<String, Object> timestamp = new HashMap<>();
                                                    timestamp.put("time", FieldValue.serverTimestamp());
                                                    final int finalY = y;
                                                    firebaseFirestore.collection("PRODUCTS").document(productID).collection("QUANTITY").document(quantityDocumentName).set(timestamp)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    qtyIds.add(quantityDocumentName);

                                                                    if (finalY + 1 == finalQty - initialQty) {

                                                                        firebaseFirestore.collection("PRODUCTS").document(productID).collection("QUANTITY").orderBy("time", Query.Direction.ASCENDING).limit(stockQty).get()
                                                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                        if (task.isSuccessful()) {

                                                                                            List<String> serverQuantity = new ArrayList<>();

                                                                                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                                                                                                serverQuantity.add(queryDocumentSnapshot.getId());

                                                                                            }
                                                                                            long availableQty = 0;
                                                                                            for (String qtyId : qtyIds) {
                                                                                                if (!serverQuantity.contains(qtyId)) {
                                                                                                    DeliveryActivity.cartItemModelList.get(position).setQtyError(true);
                                                                                                    DeliveryActivity.cartItemModelList.get(position).setMaxQuantity(availableQty);
                                                                                                    Toast.makeText(itemView.getContext(), "Sorry! All products may not be available in required quantity", Toast.LENGTH_SHORT).show();
                                                                                                } else {
                                                                                                    availableQty++;
                                                                                                }
                                                                                            }
                                                                                            DeliveryActivity.cartAdapter.notifyDataSetChanged();
                                                                                        } else {
                                                                                            String error = task.getException().getMessage();
                                                                                            Toast.makeText(itemView.getContext(), error, Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                        DeliveryActivity.loadingDialog.dismiss();
                                                                                    }
                                                                                });
                                                                    }
                                                                }
                                                            });
                                                }
                                            } else if (initialQty > finalQty) {
                                                for (int x = 0; x < initialQty - finalQty; x++) {
                                                    final String qtyId = qtyIds.get(qtyIds.size() - 1 - x);
                                                    final int finalX = x;
                                                    firebaseFirestore.collection("PRODUCTS").document(productID).collection("QUANTITY").document(qtyId).delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    qtyIds.remove(qtyId);
                                                                    DeliveryActivity.cartAdapter.notifyDataSetChanged();
                                                                    if (finalX + 1 == initialQty - finalQty){
                                                                        DeliveryActivity.loadingDialog.dismiss();
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(itemView.getContext(), "Max Quantity : " + maxQuantity.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                                quantityDialog.dismiss();
                            }
                        });
                        quantityDialog.show();
                    }
                });

                if (offersAppliedNo > 0) {
                    offersApplied.setVisibility(View.VISIBLE);
                    String offerDiscountedAmount = String.valueOf(Long.valueOf(cuttedPriceText) - Long.valueOf(productPriceText));
                    offersApplied.setText("Offer applied - Rs." + offerDiscountedAmount + "/-");
                } else {
                    offersApplied.setVisibility(View.INVISIBLE);
                }

            } else {
                productPrice.setText("Out of Stock");
                productPrice.setTextColor(itemView.getContext().getResources().getColor(R.color.colorPrimary));
                cuttedPrice.setText("");
                couponRedemptionLayout.setVisibility(View.GONE);
                productQuantity.setVisibility(View.INVISIBLE);
                freeCoupons.setVisibility(View.INVISIBLE);
                couponsApplied.setVisibility(View.GONE);
                offersApplied.setVisibility(View.GONE);
                freeCouponIcon.setVisibility(View.INVISIBLE);
            }

            if (showDeleteBtn) {
                deleteBtn.setVisibility(View.VISIBLE);
            } else {
                deleteBtn.setVisibility(View.GONE);
            }

            redeemBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (RewardsModel rewardsModel : DBQueries.rewardsModelList) {
                        if (rewardsModel.getCouponId().equals(cartItemModelList.get(position).getSelectedCouponId())) {
                            rewardsModel.setAlreadyUsed(false);
                        }
                    }
                    checkCouponPriceDialog.show();
                }
            });

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(cartItemModelList.get(position).getSelectedCouponId())) {
                        for (RewardsModel rewardsModel : DBQueries.rewardsModelList) {
                            if (rewardsModel.getCouponId().equals(cartItemModelList.get(position).getSelectedCouponId())) {
                                rewardsModel.setAlreadyUsed(false);
                            }
                        }
                    }
                    if (!ProductDetailsActivity.running_cart_query) {
                        ProductDetailsActivity.running_cart_query = true;
                        DBQueries.removeFromCart(position, itemView.getContext(), cartTotalAmount);
                    }
                }
            });
        }

        private void showDialogRecyclerView() {
            if (couponsRecyclerView.getVisibility() == View.GONE) {
                couponsRecyclerView.setVisibility(View.VISIBLE);
                selectedCoupon.setVisibility(View.GONE);
            } else {
                couponsRecyclerView.setVisibility(View.GONE);
                selectedCoupon.setVisibility(View.VISIBLE);
            }
        }
    }

    class CartTotalAmountViewholder extends RecyclerView.ViewHolder {

        private TextView totalItems, totalItemPrice, deliveryPrice, totalAmount, savedAmount;

        public CartTotalAmountViewholder(@NonNull View itemView) {
            super(itemView);

            totalItems = itemView.findViewById(R.id.total_items);
            totalItemPrice = itemView.findViewById(R.id.total_items_price);
            deliveryPrice = itemView.findViewById(R.id.delivery_price);
            totalAmount = itemView.findViewById(R.id.total_price);
            savedAmount = itemView.findViewById(R.id.saved_amount);
        }

        private void setTotalAmount(int totalItemText, int totalItemPriceText, String deliveryPriceText, int totalAmountText, int savedAmountText) {
            totalItems.setText("Price (" + totalItemText + " items)");
            totalItemPrice.setText("Rs." + totalItemPriceText + "/-");
            if (deliveryPriceText.equals("FREE")) {
                deliveryPrice.setText(deliveryPriceText);
            } else {
                deliveryPrice.setText("Rs." + deliveryPriceText + "/-");
            }
            totalAmount.setText("Rs." + totalAmountText + "/-");
            cartTotalAmount.setText("Rs." + totalAmountText + "/-");
            savedAmount.setText("You saved Rs." + savedAmountText + "/- on this order");
            LinearLayout parent = (LinearLayout) cartTotalAmount.getParent().getParent();

            if (totalItemPriceText == 0) {
                if (DeliveryActivity.fromCart) {
                    cartItemModelList.remove(cartItemModelList.size() - 1);
                    DeliveryActivity.cartItemModelList.remove(cartItemModelList.size() - 1);
                }
                if (showDeleteBtn) {
                    cartItemModelList.remove(cartItemModelList.size() - 1);
                }
                parent.setVisibility(View.GONE);
            } else {
                parent.setVisibility(View.VISIBLE);
            }
        }
    }
}
