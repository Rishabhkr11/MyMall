package rishabh.example.mymall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static rishabh.example.mymall.DBQueries.loadWishList;
import static rishabh.example.mymall.MainActivity.showCart;
import static rishabh.example.mymall.RegisterActivity.setSignUpFragment;

public class ProductDetailsActivity extends AppCompatActivity {

    public static boolean running_wishlist_query = false;
    public static boolean running_rating_query = false;
    public static boolean running_cart_query = false;
    public static Activity productDetailsActivity;

    public static boolean fromSearch = false;

    private ViewPager productImagesViewPager;
    private TextView productTitle, averageRatingMiniView, totalRatingMiniView, productPrice, cuttedPrice, tvCodIndicator, rewardTitle, rewardBody;
    private ImageView codIndicator;
    private TabLayout viewPagerIndicator;
    private LinearLayout couponRedemptionLayout;
    private Button couponRedeemBtn;
    private String productOriginalPrice;

    ////Product Description

    private ViewPager productDetailsViewPager;
    private TabLayout productDetailsTabLayout;
    private ConstraintLayout productDetailsOnlyContainer, productDetailsTabsContainer;
    private String productDescription, productOtherDetails;
    private TextView productOnlyDescriptionBody;
    private List<ProductSpecificationModel> productSpecificationModelList = new ArrayList<>();

    ////Product Description

    //////Rating layout

    public static int initialRating;
    public static LinearLayout rateNowContainer;
    private LinearLayout ratingsProgressBarContainer;
    private TextView totalRatings, totalRatingsFigure, averageRating;
    private LinearLayout ratingsNoContainer;

    //////Rating layout

    private Button buyNowBtn;
    private LinearLayout addToCartBtn;
    public static MenuItem cartItem;

    public static FloatingActionButton addToWishlistBtn;
    public static boolean ALREADY_ADDED_TO_WISHLIST = false;
    public static boolean ALREADY_ADDED_TO_CART = false;

    private FirebaseFirestore firebaseFirestore;

    /////couponDialog

    private TextView couponTitle, couponBody, couponExpiryDate, discountedPrice, originalPrice;
    private RecyclerView couponsRecyclerView;
    private LinearLayout selectedCoupon;

    /////couponDialog

    private Dialog signInDialog;
    private Dialog loadingDialog;
    private FirebaseUser currentUser;
    public static String productID;
    private TextView badgeCount;
    private boolean inStock = false;

    private DocumentSnapshot documentSnapshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        productImagesViewPager = findViewById(R.id.product_images_view_pager);
        viewPagerIndicator = findViewById(R.id.viewpager_indicator);
        addToWishlistBtn = findViewById(R.id.add_to_wishlist_btn);
        productDetailsViewPager = findViewById(R.id.product_details_viewpager);
        productDetailsTabLayout = findViewById(R.id.product_details_tablayout);
        buyNowBtn = findViewById(R.id.buy_now_btn);
        couponRedeemBtn = findViewById(R.id.coupon_redemption_btn);
        productTitle = findViewById(R.id.product_title);
        averageRatingMiniView = findViewById(R.id.tv_product_rating_miniview);
        totalRatingMiniView = findViewById(R.id.total_ratings_miniview);
        productPrice = findViewById(R.id.product_price_wishlist);
        cuttedPrice = findViewById(R.id.cutted_price_wishlist);
        codIndicator = findViewById(R.id.cod_indicator_imageview);
        tvCodIndicator = findViewById(R.id.tv_cod_indicator);
        rewardTitle = findViewById(R.id.reward_title);
        rewardBody = findViewById(R.id.reward_body);
        productDetailsOnlyContainer = findViewById(R.id.product_details_container);
        productDetailsTabsContainer = findViewById(R.id.product_details_tabs_container);
        productOnlyDescriptionBody = findViewById(R.id.product_details_body);
        totalRatings = findViewById(R.id.total_ratings);
        ratingsNoContainer = findViewById(R.id.ratings_numbers_container);
        totalRatingsFigure = findViewById(R.id.total_ratings_figure);
        ratingsProgressBarContainer = findViewById(R.id.ratings_progress_bar_container);
        averageRating = findViewById(R.id.average_rating);
        addToCartBtn = findViewById(R.id.add_to_cart_btn);
        couponRedemptionLayout = findViewById(R.id.coupan_reedemtion_layout);

        initialRating = -1;

        ////loading dialog
        loadingDialog = new Dialog(ProductDetailsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.show();
        ////loading dialog

        /////coupon Dialog

        final Dialog checkCouponPriceDialog = new Dialog(ProductDetailsActivity.this);
        checkCouponPriceDialog.setContentView(R.layout.coupon_redeem_dialog);
        checkCouponPriceDialog.setCancelable(true);
        checkCouponPriceDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ImageView toggleRecyclerView = checkCouponPriceDialog.findViewById(R.id.toggle_recyclerview);
        couponsRecyclerView = checkCouponPriceDialog.findViewById(R.id.coupons_recyclerview);
        selectedCoupon = checkCouponPriceDialog.findViewById(R.id.selected_coupon);
        couponTitle = checkCouponPriceDialog.findViewById(R.id.coupon_title);
        couponBody = checkCouponPriceDialog.findViewById(R.id.coupon_body);
        couponExpiryDate = checkCouponPriceDialog.findViewById(R.id.coupon_validity);

        originalPrice = checkCouponPriceDialog.findViewById(R.id.original_price);
        discountedPrice = checkCouponPriceDialog.findViewById(R.id.discounted_price);

        LinearLayoutManager layoutManager = new LinearLayoutManager(ProductDetailsActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        couponsRecyclerView.setLayoutManager(layoutManager);

        toggleRecyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogRecyclerView();
            }
        });

        couponRedeemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCouponPriceDialog.show();
            }
        });

        /////coupon Dialog

        firebaseFirestore = FirebaseFirestore.getInstance();

        final List<String> productImages = new ArrayList<>();
        productID = getIntent().getStringExtra("PRODUCT_ID");
        firebaseFirestore.collection("PRODUCTS").document(productID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            documentSnapshot = task.getResult();

                            firebaseFirestore.collection("PRODUCTS").document(productID).collection("QUANTITY").orderBy("time", Query.Direction.ASCENDING).get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (long x = 1; x < (long) documentSnapshot.get("no_of_product_images") + 1; x++) {
                                                    productImages.add(documentSnapshot.get("product_image_" + x).toString());
                                                }
                                                ProductImagesAdapter productImagesAdapter = new ProductImagesAdapter(productImages);
                                                productImagesViewPager.setAdapter(productImagesAdapter);

                                                productTitle.setText(documentSnapshot.get("product_title").toString());
                                                averageRatingMiniView.setText(documentSnapshot.get("average_rating").toString());
                                                totalRatingMiniView.setText("(" + (long) documentSnapshot.get("total_ratings") + ") Ratings");
                                                productPrice.setText("Rs." + documentSnapshot.get("product_price").toString() + "/-");

                                                /////for coupon dialog
                                                originalPrice.setText(productPrice.getText());
                                                productOriginalPrice = documentSnapshot.get("product_price").toString();
                                                MyRewardsAdapter myRewardsAdapter = new MyRewardsAdapter(DBQueries.rewardsModelList, true, couponsRecyclerView, selectedCoupon, productOriginalPrice, couponTitle, couponExpiryDate, couponBody, discountedPrice);
                                                couponsRecyclerView.setAdapter(myRewardsAdapter);
                                                myRewardsAdapter.notifyDataSetChanged();
                                                /////for coupon dialog

                                                cuttedPrice.setText("Rs." + documentSnapshot.get("cutted_price").toString() + "/-");
                                                if ((boolean) documentSnapshot.get("COD")) {
                                                    codIndicator.setVisibility(View.VISIBLE);
                                                    tvCodIndicator.setVisibility(View.VISIBLE);
                                                } else {
                                                    codIndicator.setVisibility(View.INVISIBLE);
                                                    tvCodIndicator.setVisibility(View.INVISIBLE);
                                                }
                                                rewardTitle.setText((long) documentSnapshot.get("free_coupons") + " " + documentSnapshot.get("free_coupon_title").toString());
                                                rewardBody.setText(documentSnapshot.get("free_coupon_body").toString());

                                                if ((boolean) documentSnapshot.get("use_tab_layout")) {
                                                    productDetailsTabsContainer.setVisibility(View.VISIBLE);
                                                    productDetailsOnlyContainer.setVisibility(View.GONE);
                                                    productDescription = documentSnapshot.get("product_description").toString();
                                                    productOtherDetails = documentSnapshot.get("product_other_details").toString();

                                                    for (long x = 1; x < (long) documentSnapshot.get("total_spec_titles") + 1; x++) {

                                                        productSpecificationModelList.add(new ProductSpecificationModel(0, documentSnapshot.get("spec_title_" + x).toString()));
                                                        for (long y = 1; y < (long) documentSnapshot.get("spec_title_" + x + "_total_fields") + 1; y++) {
                                                            productSpecificationModelList.add(new ProductSpecificationModel(1, documentSnapshot.get("spec_title_" + x + "_field_" + y + "_name").toString(), documentSnapshot.get("spec_title_" + x + "_field_" + y + "_value").toString()));
                                                        }

                                                    }
                                                } else {
                                                    productDetailsTabsContainer.setVisibility(View.GONE);
                                                    productDetailsOnlyContainer.setVisibility(View.VISIBLE);
                                                    productOnlyDescriptionBody.setText(documentSnapshot.get("product_description").toString());
                                                }

                                                totalRatings.setText((long) documentSnapshot.get("total_ratings") + " Ratings");

                                                for (int x = 0; x < 5; x++) {
                                                    TextView rating = (TextView) ratingsNoContainer.getChildAt(x);
                                                    rating.setText(String.valueOf((long) documentSnapshot.get((5 - x) + "_star")));

                                                    ProgressBar progressBar = (ProgressBar) ratingsProgressBarContainer.getChildAt(x);
                                                    int maxProgress = Integer.parseInt(String.valueOf((long) documentSnapshot.get("total_ratings")));
                                                    progressBar.setMax(maxProgress);
                                                    progressBar.setProgress(Integer.parseInt(String.valueOf((long) documentSnapshot.get((5 - x) + "_star"))));
                                                }
                                                totalRatingsFigure.setText(String.valueOf((long) documentSnapshot.get("total_ratings")));
                                                averageRating.setText(documentSnapshot.get("average_rating").toString());
                                                productDetailsViewPager.setAdapter(new ProductDetailsAdapter(getSupportFragmentManager(), productDetailsTabLayout.getTabCount(), productDescription, productOtherDetails, productSpecificationModelList));

                                                if (currentUser != null) {
                                                    if (DBQueries.myRating.size() == 0) {
                                                        DBQueries.loadRatingList(ProductDetailsActivity.this);
                                                    }
                                                    if (DBQueries.cartList.size() == 0) {
                                                        DBQueries.loadCartList(ProductDetailsActivity.this, loadingDialog, false, badgeCount, new TextView(ProductDetailsActivity.this));
                                                    }
                                                    if (DBQueries.wishList.size() == 0) {
                                                        DBQueries.loadWishList(ProductDetailsActivity.this, loadingDialog, false);
                                                    }
                                                    if (DBQueries.rewardsModelList.size() == 0) {
                                                        DBQueries.loadRewards(ProductDetailsActivity.this, loadingDialog, false);
                                                    }
                                                    if (DBQueries.cartList.size() != 0 && DBQueries.rewardsModelList.size() != 0 && DBQueries.wishList.size() != 0) {
                                                        loadingDialog.dismiss();
                                                    }
                                                } else {
                                                    loadingDialog.dismiss();
                                                }

                                                if (DBQueries.myRatedIds.contains(productID)) {
                                                    int index = DBQueries.myRatedIds.indexOf(productID);
                                                    initialRating = Integer.parseInt(String.valueOf(DBQueries.myRating.get(index))) - 1;
                                                    setRating(initialRating);
                                                }

                                                if (DBQueries.cartList.contains(productID)) {
                                                    ALREADY_ADDED_TO_CART = true;
                                                } else {
                                                    ALREADY_ADDED_TO_CART = false;
                                                }

                                                if (DBQueries.wishList.contains(productID)) {
                                                    ALREADY_ADDED_TO_WISHLIST = true;
                                                    addToWishlistBtn.setSupportImageTintList(getResources().getColorStateList(R.color.colorPrimary));
                                                } else {
                                                    addToWishlistBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#9e9e9e")));
                                                    ALREADY_ADDED_TO_WISHLIST = false;
                                                }

                                                if (task.getResult().getDocuments().size() < (long) documentSnapshot.get("stock_quantity")) {
                                                    inStock = true;
                                                    buyNowBtn.setVisibility(View.VISIBLE);
                                                    addToCartBtn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            if (currentUser == null) {
                                                                signInDialog.show();
                                                            } else {
                                                                if (!running_cart_query) {
                                                                    running_cart_query = true;
                                                                    if (ALREADY_ADDED_TO_CART) {
                                                                        running_cart_query = false;
                                                                        Toast.makeText(ProductDetailsActivity.this, "Already added to cart!", Toast.LENGTH_SHORT).show();
                                                                    } else {
                                                                        Map<String, Object> addProduct = new HashMap<>();
                                                                        addProduct.put("product_ID_" + String.valueOf(DBQueries.cartList.size()), productID);
                                                                        addProduct.put("list_size", (long) (DBQueries.cartList.size() + 1));

                                                                        firebaseFirestore.collection("USERS").document(currentUser.getUid()).collection("USER_DATA")
                                                                                .document("MY_CART").update(addProduct).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    if (DBQueries.cartItemModelList.size() != 0) {
                                                                                        DBQueries.cartItemModelList.add(0, new CartItemModel(documentSnapshot.getBoolean("COD")
                                                                                                , CartItemModel.CART_ITEM
                                                                                                , productID
                                                                                                , (long) documentSnapshot.get("free_coupons")
                                                                                                , (long) 1
                                                                                                , (long) documentSnapshot.get("offers_applied")
                                                                                                , (long) 0
                                                                                                , documentSnapshot.get("product_image_1").toString()
                                                                                                , documentSnapshot.get("product_price").toString()
                                                                                                , documentSnapshot.get("cutted_price").toString()
                                                                                                , documentSnapshot.get("product_title").toString()
                                                                                                , inStock
                                                                                                , (long) documentSnapshot.get("max_quantity")
                                                                                                , (long) documentSnapshot.get("stock_quantity")));
                                                                                    }

                                                                                    ALREADY_ADDED_TO_CART = true;
                                                                                    DBQueries.cartList.add(productID);
                                                                                    Toast.makeText(ProductDetailsActivity.this, "Added to Cart!", Toast.LENGTH_SHORT).show();
                                                                                    invalidateOptionsMenu();
                                                                                    running_cart_query = false;

                                                                                } else {
                                                                                    running_cart_query = false;
                                                                                    String error = task.getException().getMessage();
                                                                                    Toast.makeText(ProductDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    inStock = false;
                                                    buyNowBtn.setVisibility(View.GONE);
                                                    TextView outOfStock = (TextView) addToCartBtn.getChildAt(0);
                                                    outOfStock.setText("Out of Stock");
                                                    outOfStock.setTextColor(getResources().getColor(R.color.colorPrimary));
                                                    outOfStock.setCompoundDrawables(null, null, null, null);
                                                }
                                            } else {
                                                String error = task.getException().getMessage();
                                                Toast.makeText(ProductDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            loadingDialog.dismiss();
                            String error = task.getException().getMessage();
                            Toast.makeText(ProductDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        viewPagerIndicator.setupWithViewPager(productImagesViewPager, true);

        addToWishlistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser == null) {
                    signInDialog.show();
                } else {
                    if (!running_wishlist_query) {
                        running_wishlist_query = true;
                        if (ALREADY_ADDED_TO_WISHLIST) {
                            int index = DBQueries.wishList.indexOf(productID);
                            DBQueries.removeFromWishList(index, ProductDetailsActivity.this);
                            addToWishlistBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#9e9e9e")));
                        } else {
                            addToWishlistBtn.setSupportImageTintList(getResources().getColorStateList(R.color.colorPrimary));
                            Map<String, Object> addProduct = new HashMap<>();
                            addProduct.put("product_ID_" + String.valueOf(DBQueries.wishList.size()), productID);
                            addProduct.put("list_size", (long) (DBQueries.wishList.size() + 1));

                            firebaseFirestore.collection("USERS").document(currentUser.getUid()).collection("USER_DATA")
                                    .document("MY_WISHLIST").update(addProduct).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        if (DBQueries.wishlistModelList.size() != 0) {
                                            DBQueries.wishlistModelList.add(new WishlistModel(productID, documentSnapshot.get("product_image_1").toString()
                                                    , (long) documentSnapshot.get("free_coupons")
                                                    , (long) documentSnapshot.get("total_ratings")
                                                    , documentSnapshot.get("product_title").toString()
                                                    , documentSnapshot.get("average_rating").toString()
                                                    , documentSnapshot.get("product_price").toString()
                                                    , documentSnapshot.get("cutted_price").toString()
                                                    , (boolean) documentSnapshot.get("COD")
                                                    , inStock));
                                        }

                                        ALREADY_ADDED_TO_WISHLIST = true;
                                        addToWishlistBtn.setSupportImageTintList(getResources().getColorStateList(R.color.colorPrimary));
                                        DBQueries.wishList.add(productID);
                                        Toast.makeText(ProductDetailsActivity.this, "Added to wish list", Toast.LENGTH_SHORT).show();

                                    } else {
                                        addToWishlistBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#9e9e9e")));

                                        String error = task.getException().getMessage();
                                        Toast.makeText(ProductDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                                    }
                                    running_wishlist_query = false;
                                }
                            });
                        }
                    }
                }
            }
        });

        productDetailsViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(productDetailsTabLayout));
        productDetailsTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                productDetailsViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        /////rating layout

        rateNowContainer = findViewById(R.id.rate_now_container);

        for (int x = 0; x < rateNowContainer.getChildCount(); x++) {
            final int starPosition = x;
            rateNowContainer.getChildAt(x).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentUser == null) {
                        signInDialog.show();
                    } else {
                        if (starPosition != initialRating) {
                            if (!running_rating_query) {
                                running_rating_query = true;
                                setRating(starPosition);

                                Map<String, Object> updateRating = new HashMap<>();
                                if (DBQueries.myRatedIds.contains(productID)) {

                                    TextView oldRating = (TextView) ratingsNoContainer.getChildAt(5 - initialRating - 1);
                                    TextView finalRating = (TextView) ratingsNoContainer.getChildAt(5 - starPosition - 1);

                                    updateRating.put(initialRating + 1 + "_star", Long.parseLong(oldRating.getText().toString()) - 1);
                                    updateRating.put(starPosition + 1 + "_star", Long.parseLong(finalRating.getText().toString()) + 1);
                                    updateRating.put("average_rating", calculateAverageRating((long) starPosition - initialRating, true));

                                } else {
                                    updateRating.put(starPosition + 1 + "_star", (long) documentSnapshot.get(starPosition + 1 + "_star") + 1);
                                    updateRating.put("average_rating", calculateAverageRating((long) starPosition + 1, false));
                                    updateRating.put("total_ratings", (long) documentSnapshot.get("total_ratings") + 1);
                                }
                                firebaseFirestore.collection("PRODUCTS").document(productID)
                                        .update(updateRating).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Map<String, Object> myRating = new HashMap<>();
                                            if (DBQueries.myRatedIds.contains(productID)) {
                                                myRating.put("rating_" + DBQueries.myRatedIds.indexOf(productID), (long) starPosition + 1);
                                            } else {
                                                myRating.put("list_size", (long) DBQueries.myRatedIds.size() + 1);
                                                myRating.put("product_ID_" + DBQueries.myRatedIds.size(), productID);
                                                myRating.put("rating_" + DBQueries.myRatedIds.size(), (long) starPosition + 1);
                                            }

                                            firebaseFirestore.collection("USERS").document(currentUser.getUid()).collection("USER_DATA").document("MY_RATINGS")
                                                    .update(myRating).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {

                                                        if (DBQueries.myRatedIds.contains(productID)) {
                                                            DBQueries.myRating.set(DBQueries.myRatedIds.indexOf(productID), (long) (starPosition + 1));

                                                            TextView oldRating = (TextView) ratingsNoContainer.getChildAt(5 - initialRating - 1);
                                                            TextView finalRating = (TextView) ratingsNoContainer.getChildAt(5 - starPosition - 1);

                                                            oldRating.setText(String.valueOf(Integer.parseInt(oldRating.getText().toString()) - 1));
                                                            finalRating.setText(String.valueOf(Integer.parseInt(finalRating.getText().toString()) + 1));
                                                        } else {

                                                            DBQueries.myRatedIds.add(productID);
                                                            DBQueries.myRating.add((long) (starPosition + 1));

                                                            TextView rating = (TextView) ratingsNoContainer.getChildAt(5 - starPosition - 1);
                                                            rating.setText(String.valueOf(Integer.parseInt(rating.getText().toString()) + 1));

                                                            totalRatingMiniView.setText("(" + ((long) documentSnapshot.get("total_ratings") + 1) + ") Ratings");
                                                            totalRatings.setText((long) documentSnapshot.get("total_ratings") + 1 + " Ratings");
                                                            totalRatingsFigure.setText(String.valueOf((long) documentSnapshot.get("total_ratings") + 1));

                                                            Toast.makeText(ProductDetailsActivity.this, "Thanks for Rating!", Toast.LENGTH_SHORT).show();
                                                        }

                                                        for (int x = 0; x < 5; x++) {
                                                            TextView ratingFigures = (TextView) ratingsNoContainer.getChildAt(x);

                                                            ProgressBar progressBar = (ProgressBar) ratingsProgressBarContainer.getChildAt(x);
                                                            int maxProgress = Integer.parseInt(String.valueOf(totalRatingsFigure.getText().toString()));
                                                            progressBar.setMax(maxProgress);
                                                            progressBar.setProgress(Integer.parseInt(ratingFigures.getText().toString()));
                                                        }
                                                        initialRating = starPosition;
                                                        averageRating.setText(calculateAverageRating((long) 0, true));
                                                        averageRatingMiniView.setText(calculateAverageRating((long) 0, true));

                                                        if (DBQueries.wishList.contains(productID) && DBQueries.wishlistModelList.size() != 0) {
                                                            int index = DBQueries.wishList.indexOf(productID);
                                                            DBQueries.wishlistModelList.get(index).setRating(averageRating.getText().toString());
                                                            DBQueries.wishlistModelList.get(index).setTotalRatings(Long.parseLong(totalRatingsFigure.getText().toString()));
                                                        }

                                                    } else {
                                                        setRating(initialRating);
                                                        String error = task.getException().getMessage();
                                                        Toast.makeText(ProductDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                                                    }
                                                    running_rating_query = false;
                                                }
                                            });
                                        } else {
                                            running_rating_query = false;
                                            setRating(initialRating);
                                            String error = task.getException().getMessage();
                                            Toast.makeText(ProductDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }

        /////rating layout

        buyNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser == null) {
                    signInDialog.show();
                } else {
                    DeliveryActivity.fromCart = false;
                    loadingDialog.show();
                    productDetailsActivity = ProductDetailsActivity.this;
                    DeliveryActivity.cartItemModelList = new ArrayList<>();
                    DeliveryActivity.cartItemModelList.add(new CartItemModel(documentSnapshot.getBoolean("COD")
                            , CartItemModel.CART_ITEM
                            , productID
                            , (long) documentSnapshot.get("free_coupons")
                            , (long) 1
                            , (long) documentSnapshot.get("offers_applied")
                            , (long) 0
                            , documentSnapshot.get("product_image_1").toString()
                            , documentSnapshot.get("product_price").toString()
                            , documentSnapshot.get("cutted_price").toString()
                            , documentSnapshot.get("product_title").toString()
                            , inStock
                            , (long) documentSnapshot.get("max_quantity")
                            , (long) documentSnapshot.get("stock_quantity")));

                    DeliveryActivity.cartItemModelList.add(new CartItemModel(CartItemModel.TOTAL_AMOUNT));

                    if (DBQueries.addressesModelList.size() == 0) {
                        DBQueries.loadAddresses(ProductDetailsActivity.this, loadingDialog, true);
                    } else {
                        loadingDialog.dismiss();
                        Intent deliveryIntent = new Intent(ProductDetailsActivity.this, DeliveryActivity.class);
                        startActivity(deliveryIntent);
                    }
                }
            }
        });

        //////sign in dialog

        signInDialog = new Dialog(ProductDetailsActivity.this);
        signInDialog.setContentView(R.layout.sign_in_dialog);
        signInDialog.setCancelable(true);
        signInDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Button dialogSignInBtn = signInDialog.findViewById(R.id.cancel_dialog_btn);
        Button dialogSignUpBtn = signInDialog.findViewById(R.id.ok_dialog_btn);

        final Intent registerIntent = new Intent(ProductDetailsActivity.this, RegisterActivity.class);

        dialogSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInFragment.disableCloseBtn = true;
                SignUpFragment.disableCloseBtn = true;
                signInDialog.dismiss();
                setSignUpFragment = false;
                startActivity(registerIntent);
            }
        });

        dialogSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUpFragment.disableCloseBtn = true;
                SignInFragment.disableCloseBtn = true;
                signInDialog.dismiss();
                setSignUpFragment = true;
                startActivity(registerIntent);
            }
        });

        //////sign in dialog
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            couponRedemptionLayout.setVisibility(View.GONE);
        } else {
            couponRedemptionLayout.setVisibility(View.VISIBLE);
        }

        if (currentUser != null) {
            if (DBQueries.myRating.size() == 0) {
                DBQueries.loadRatingList(ProductDetailsActivity.this);
            }
            if (DBQueries.wishList.size() == 0) {
                DBQueries.loadWishList(ProductDetailsActivity.this, loadingDialog, false);
            }
            if (DBQueries.rewardsModelList.size() == 0) {
                DBQueries.loadRewards(ProductDetailsActivity.this, loadingDialog, false);
            }
            if (DBQueries.cartList.size() != 0 && DBQueries.rewardsModelList.size() != 0 && DBQueries.wishList.size() != 0) {
                loadingDialog.dismiss();
            }
        } else {
            loadingDialog.dismiss();
        }

        if (DBQueries.myRatedIds.contains(productID)) {
            int index = DBQueries.myRatedIds.indexOf(productID);
            initialRating = Integer.parseInt(String.valueOf(DBQueries.myRating.get(index))) - 1;
            setRating(initialRating);
        }

        if (DBQueries.cartList.contains(productID)) {
            ALREADY_ADDED_TO_CART = true;
        } else {
            ALREADY_ADDED_TO_CART = false;
        }

        if (DBQueries.wishList.contains(productID)) {
            ALREADY_ADDED_TO_WISHLIST = true;
            addToWishlistBtn.setSupportImageTintList(getResources().getColorStateList(R.color.colorPrimary));
        } else {
            addToWishlistBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#9e9e9e")));
            ALREADY_ADDED_TO_WISHLIST = false;
        }
        invalidateOptionsMenu();
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

    public static void setRating(int starPosition) {
        for (int x = 0; x < rateNowContainer.getChildCount(); x++) {
            ImageView starBtn = (ImageView) rateNowContainer.getChildAt(x);
            starBtn.setImageTintList(ColorStateList.valueOf(Color.parseColor("#bebebe")));
            if (x <= starPosition) {
                starBtn.setImageTintList(ColorStateList.valueOf(Color.parseColor("#ffbb00")));
            }
        }
    }

    private String calculateAverageRating(Long currentUserRating, boolean update) {
        Double totalStars = Double.valueOf(0);
        for (int x = 1; x < 6; x++) {
            TextView ratingNo = (TextView) ratingsNoContainer.getChildAt(5 - x);
            totalStars = totalStars + (Long.parseLong(ratingNo.getText().toString()) * x);
        }
        totalStars = totalStars + currentUserRating;
        if (update) {
            return String.valueOf(totalStars / Long.parseLong(totalRatingsFigure.getText().toString())).substring(0, 3);
        } else {
            return String.valueOf(totalStars / (Long.parseLong(totalRatingsFigure.getText().toString()) + 1)).substring(0, 3);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_and_cart_icon, menu);

        cartItem = menu.findItem(R.id.main_cart_icon);
        cartItem.setActionView(R.layout.badge_layout);
        ImageView badgeIcon = cartItem.getActionView().findViewById(R.id.badge_icon);
        badgeIcon.setImageResource(R.mipmap.cart_white);
        badgeCount = cartItem.getActionView().findViewById(R.id.badge_count);

        if (currentUser != null) {
            if (DBQueries.cartList.size() == 0) {
                DBQueries.loadCartList(ProductDetailsActivity.this, loadingDialog, false, badgeCount, new TextView(ProductDetailsActivity.this));
            } else {
                badgeCount.setVisibility(View.VISIBLE);
                if (DBQueries.cartList.size() < 99) {
                    badgeCount.setText(String.valueOf(DBQueries.cartList.size()));
                } else {
                    badgeCount.setText("99+");
                }
            }
        }
        cartItem.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser == null) {
                    signInDialog.show();
                } else {
                    Intent cartIntent = new Intent(ProductDetailsActivity.this, MainActivity.class);
                    showCart = true;
                    startActivity(cartIntent);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            productDetailsActivity = null;
            finish();
            return true;
        } else if (id == R.id.main_search_icon) {
            if (fromSearch) {
                finish();
            } else {
                Intent searchIntent = new Intent(this, SearchActivity.class);
                startActivity(searchIntent);
            }
            return true;
        } else if (id == R.id.main_cart_icon) {
            if (currentUser == null) {
                signInDialog.show();
            } else {
                Intent cartIntent = new Intent(ProductDetailsActivity.this, MainActivity.class);
                showCart = true;
                startActivity(cartIntent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        productDetailsActivity = null;
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fromSearch = false;
    }
}