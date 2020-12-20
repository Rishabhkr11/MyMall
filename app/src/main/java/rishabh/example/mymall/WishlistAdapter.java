package rishabh.example.mymall;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.Viewholder> {

    private List<WishlistModel> wishlistModelList;
    private Boolean wishlist;
    private int lastPosition = -1;
    private boolean fromSearch;

    public boolean isFromSearch() {
        return fromSearch;
    }

    public void setFromSearch(boolean fromSearch) {
        this.fromSearch = fromSearch;
    }

    public WishlistAdapter(List<WishlistModel> wishlistModelList, Boolean wishlist) {
        this.wishlistModelList = wishlistModelList;
        this.wishlist = wishlist;
    }

    public List<WishlistModel> getWishlistModelList() {
        return wishlistModelList;
    }

    public void setWishlistModelList(List<WishlistModel> wishlistModelList) {
        this.wishlistModelList = wishlistModelList;
    }

    @NonNull
    @Override
    public WishlistAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wishlist_item_layout, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistAdapter.Viewholder viewholder, int position) {
        String productID = wishlistModelList.get(position).getProductID();
        String resource = wishlistModelList.get(position).getProductImage();
        String title = wishlistModelList.get(position).getProductTitle();
        long freeCoupons = wishlistModelList.get(position).getFreeCoupons();
        String rating = wishlistModelList.get(position).getRating();
        long totalRatings = wishlistModelList.get(position).getTotalRatings();
        String productPrice = wishlistModelList.get(position).getProductPrice();
        String cuttedPrice = wishlistModelList.get(position).getCuttedPrice();
        boolean paymentMethod = wishlistModelList.get(position).isCOD();
        boolean inStock = wishlistModelList.get(position).isInStock();

        viewholder.setData(productID, resource, title, freeCoupons, rating, totalRatings, productPrice, cuttedPrice, paymentMethod, position, inStock);

        if (lastPosition < position) {
            Animation animation = AnimationUtils.loadAnimation(viewholder.itemView.getContext(), R.anim.fade_in);
            viewholder.itemView.setAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return wishlistModelList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {

        private ImageView productImage, couponIcon;
        private TextView productTitle, freeCoupons, productPrice, cuttedPrice, paymentMethod, rating, totalRatings;
        private View priceCut;
        private ImageButton deleteBtn;

        public Viewholder(@NonNull View itemView) {
            super(itemView);

            productImage = itemView.findViewById(R.id.product_image_wishlist);
            couponIcon = itemView.findViewById(R.id.coupon_icon_wishlist);
            productTitle = itemView.findViewById(R.id.product_title_wishlist);
            freeCoupons = itemView.findViewById(R.id.free_coupons);
            productPrice = itemView.findViewById(R.id.product_price_wishlist);
            cuttedPrice = itemView.findViewById(R.id.cutted_price_wishlist);
            paymentMethod = itemView.findViewById(R.id.payment_method);
            rating = itemView.findViewById(R.id.tv_product_rating_miniview);
            totalRatings = itemView.findViewById(R.id.total_ratings_wishlist);
            priceCut = itemView.findViewById(R.id.price_cut);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
        }

        private void setData(final String productID, String resource, String title, long freeCouponsNo, String averageRate, long totalRatingsNo, String price, String cuttedPriceValue, boolean COD, final int index, boolean inStock){
            Glide.with(itemView.getContext()).load(resource).apply(new RequestOptions().placeholder(R.mipmap.icon_place_holder)).into(productImage);
            productTitle.setText(title);
            if (freeCouponsNo != 0 && inStock){
                couponIcon.setVisibility(View.VISIBLE);
                if (freeCouponsNo == 1) {
                    freeCoupons.setText("Free " + freeCouponsNo + " Coupon");
                }
                else {
                    freeCoupons.setText("Free " + freeCouponsNo + " Coupons");
                }
            }
            else {
                couponIcon.setVisibility(View.INVISIBLE);
                freeCoupons.setVisibility(View.INVISIBLE);
            }
            LinearLayout linearLayout = (LinearLayout) rating.getParent();
            if (inStock) {
                rating.setVisibility(View.VISIBLE);
                totalRatings.setVisibility(View.VISIBLE);
                productPrice.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
                cuttedPrice.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.VISIBLE);

                rating.setText(averageRate);
                totalRatings.setText("(" + totalRatingsNo + ") ratings");
                productPrice.setText("Rs." + price + "/-");
                cuttedPrice.setText("Rs." + cuttedPriceValue + "/-");
                if (COD) {
                    paymentMethod.setVisibility(View.VISIBLE);
                } else {
                    paymentMethod.setVisibility(View.INVISIBLE);
                }
            }
            else {
                linearLayout.setVisibility(View.INVISIBLE);
                rating.setVisibility(View.INVISIBLE);
                totalRatings.setVisibility(View.INVISIBLE);
                productPrice.setText("Out of Stock");
                productPrice.setTextColor(itemView.getContext().getResources().getColor(R.color.colorPrimary));
                cuttedPrice.setVisibility(View.INVISIBLE);
                paymentMethod.setVisibility(View.INVISIBLE);
            }

            if (wishlist){
                deleteBtn.setVisibility(View.VISIBLE);
            }
            else {
                deleteBtn.setVisibility(View.GONE);
            }

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!ProductDetailsActivity.running_wishlist_query) {
                        ProductDetailsActivity.running_wishlist_query = true;
                        DBQueries.removeFromWishList(index, itemView.getContext());
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fromSearch){
                        ProductDetailsActivity.fromSearch = true;
                    }
                    Intent intent = new Intent(itemView.getContext(), ProductDetailsActivity.class);
                    intent.putExtra("PRODUCT_ID", productID);
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }
}
