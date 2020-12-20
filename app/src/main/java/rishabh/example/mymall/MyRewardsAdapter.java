package rishabh.example.mymall;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MyRewardsAdapter extends RecyclerView.Adapter<MyRewardsAdapter.Viewholder> {

    private List<RewardsModel> rewardsModelList;
    private Boolean useMiniLayout = false;
    private RecyclerView couponsRecyclerView;
    private LinearLayout selectedCoupon;
    private String productOriginalPrice;
    private TextView selectedCouponTitle, selectedCouponExpiryDate, selectedCouponBody, discountedPrice;
    private int cartItemPosition = -1;
    private List<CartItemModel> cartItemModelList;

    public MyRewardsAdapter(List<RewardsModel> rewardsModelList, Boolean useMiniLayout) {
        this.rewardsModelList = rewardsModelList;
        this.useMiniLayout = useMiniLayout;
    }

    public MyRewardsAdapter(List<RewardsModel> rewardsModelList, Boolean useMiniLayout, RecyclerView couponsRecyclerView, LinearLayout selectedCoupon, String productOriginalPrice, TextView selectedCouponTitle, TextView selectedCouponExpiryDate, TextView selectedCouponBody, TextView discountedPrice) {
        this.rewardsModelList = rewardsModelList;
        this.useMiniLayout = useMiniLayout;
        this.couponsRecyclerView = couponsRecyclerView;
        this.selectedCoupon = selectedCoupon;
        this.productOriginalPrice = productOriginalPrice;
        this.selectedCouponTitle = selectedCouponTitle;
        this.selectedCouponExpiryDate = selectedCouponExpiryDate;
        this.selectedCouponBody = selectedCouponBody;
        this.discountedPrice = discountedPrice;
    }

    public MyRewardsAdapter(int cartItemPosition, List<RewardsModel> rewardsModelList, Boolean useMiniLayout, RecyclerView couponsRecyclerView, LinearLayout selectedCoupon, String productOriginalPrice, TextView selectedCouponTitle, TextView selectedCouponExpiryDate, TextView selectedCouponBody, TextView discountedPrice, List<CartItemModel> cartItemModelList) {
        this.cartItemPosition = cartItemPosition;
        this.rewardsModelList = rewardsModelList;
        this.useMiniLayout = useMiniLayout;
        this.couponsRecyclerView = couponsRecyclerView;
        this.selectedCoupon = selectedCoupon;
        this.productOriginalPrice = productOriginalPrice;
        this.selectedCouponTitle = selectedCouponTitle;
        this.selectedCouponExpiryDate = selectedCouponExpiryDate;
        this.selectedCouponBody = selectedCouponBody;
        this.discountedPrice = discountedPrice;
        this.cartItemModelList = cartItemModelList;
    }

    @NonNull
    @Override
    public MyRewardsAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (useMiniLayout){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mini_rewards_item_layout, parent, false);
        }
        else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rewards_item_layout, parent, false);
        }
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRewardsAdapter.Viewholder viewholder, int position) {
        String couponId = rewardsModelList.get(position).getCouponId();
        String type = rewardsModelList.get(position).getType();
        Date validity = rewardsModelList.get(position).getTimestamp();
        String body = rewardsModelList.get(position).getCouponBody();
        String lowerLimit = rewardsModelList.get(position).getLowerLimit();
        String upperLimit = rewardsModelList.get(position).getUpperLimit();
        String discORamt = rewardsModelList.get(position).getDiscORamt();
        Boolean alreadyUsed = rewardsModelList.get(position).getAlreadyUsed();

        viewholder.setData(couponId, type, validity, body, upperLimit, lowerLimit, discORamt, alreadyUsed);
    }

    @Override
    public int getItemCount() {
        return rewardsModelList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {

        private TextView couponTitle, couponExpiryDate, couponBody;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            couponTitle = itemView.findViewById(R.id.coupon_title);
            couponExpiryDate = itemView.findViewById(R.id.coupon_validity);
            couponBody = itemView.findViewById(R.id.coupon_body);
        }

        private void setData(final String couponId, final String type, final Date validity, final String body, final String upperLimit, final String lowerLimit, final String discORamt, final Boolean alreadyUsed){
            if (type.equals("Discount")){
                couponTitle.setText(type);
            }
            else {
                couponTitle.setText("FLAT Rs." + discORamt + "/- OFF");
            }

            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy");

            if (alreadyUsed){
                couponExpiryDate.setText("Already Used");
                couponExpiryDate.setTextColor(itemView.getContext().getResources().getColor(R.color.colorPrimary));
                couponBody.setTextColor(Color.parseColor("#50ffffff"));
                couponTitle.setTextColor(Color.parseColor("#50ffffff"));
            }
            else {
                couponExpiryDate.setTextColor(itemView.getContext().getResources().getColor(R.color.couponPurple));
                couponExpiryDate.setText("till " + simpleDateFormat.format(validity));
                couponBody.setTextColor(Color.parseColor("#ffffff"));
                couponTitle.setTextColor(Color.parseColor("#ffffff"));
            }

            couponBody.setText(body);

            if (useMiniLayout){
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!alreadyUsed){
                            selectedCouponTitle.setText(type);
                            selectedCouponExpiryDate.setText(simpleDateFormat.format(validity));
                            selectedCouponBody.setText(body);

                            if (Long.valueOf(productOriginalPrice) > Long.valueOf(lowerLimit) && Long.valueOf(productOriginalPrice) < Long.valueOf(upperLimit)) {

                                if (type.equals("Discount")) {
                                    Long discountAmount = (Long.valueOf(productOriginalPrice) * Long.valueOf(discORamt)) / 100;
                                    discountedPrice.setText("Rs." + String.valueOf(Long.valueOf(productOriginalPrice) - discountAmount) + "/-");
                                    discountedPrice.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
                                } else {
                                    discountedPrice.setText("Rs." + String.valueOf(Long.valueOf(productOriginalPrice) - Long.valueOf(discORamt)) + "/-");
                                    discountedPrice.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
                                }
                                if (cartItemPosition != -1) {
                                    cartItemModelList.get(cartItemPosition).setSelectedCouponId(couponId);
                                }

                            } else {
                                if (cartItemPosition != -1) {
                                    cartItemModelList.get(cartItemPosition).setSelectedCouponId(null);
                                }
                                discountedPrice.setText("Invalid");
                                discountedPrice.setTextColor(itemView.getContext().getResources().getColor(R.color.colorPrimary));
                                Toast.makeText(itemView.getContext(), "Sorry! Product does not matches the coupon T&C", Toast.LENGTH_SHORT).show();
                            }

                            if (couponsRecyclerView.getVisibility() == View.GONE) {
                                couponsRecyclerView.setVisibility(View.VISIBLE);
                                selectedCoupon.setVisibility(View.GONE);
                            } else {
                                couponsRecyclerView.setVisibility(View.GONE);
                                selectedCoupon.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }
        }
    }
}
