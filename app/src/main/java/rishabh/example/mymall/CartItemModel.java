package rishabh.example.mymall;

import java.util.ArrayList;
import java.util.List;

public class CartItemModel {

    public static final int CART_ITEM = 0;
    public static final int TOTAL_AMOUNT = 1;

    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    //////cart item

    private String productPrice, cuttedPrice, productTitle, productImage, productID, selectedCouponId, discountedPrice;
    private Long freeCoupons, productQuantity, offerApplied, couponsApplied, maxQuantity, stockQuantity;
    private boolean inStock, qtyError;
    private List<String> qtyIDs;
    private boolean COD;

    public CartItemModel(boolean COD, int type, String productID, Long freeCoupons, Long productQuantity, Long offerApplied, Long couponsApplied, String productImage, String productPrice, String cuttedPrice, String productTitle, boolean inStock, Long maxQuantity, Long stockQuantity) {
        this.type = type;
        this.productID = productID;
        this.freeCoupons = freeCoupons;
        this.productQuantity = productQuantity;
        this.offerApplied = offerApplied;
        this.couponsApplied = couponsApplied;
        this.productImage = productImage;
        this.productPrice = productPrice;
        this.cuttedPrice = cuttedPrice;
        this.productTitle = productTitle;
        this.inStock = inStock;
        this.maxQuantity = maxQuantity;
        this.stockQuantity = stockQuantity;
        qtyIDs = new ArrayList<>();
        qtyError = false;
        this.COD = COD;
    }

    public boolean isCOD() {
        return COD;
    }

    public void setCOD(boolean COD) {
        this.COD = COD;
    }

    public String getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(String discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public String getSelectedCouponId() {
        return selectedCouponId;
    }

    public void setSelectedCouponId(String selectedCouponId) {
        this.selectedCouponId = selectedCouponId;
    }

    public boolean isQtyError() {
        return qtyError;
    }

    public void setQtyError(boolean qtyError) {
        this.qtyError = qtyError;
    }

    public Long getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Long stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public List<String> getQtyIDs() {
        return qtyIDs;
    }

    public void setQtyIDs(List<String> qtyIDs) {
        this.qtyIDs = qtyIDs;
    }

    public Long getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(Long maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public Long getFreeCoupons() {
        return freeCoupons;
    }

    public void setFreeCoupons(Long freeCoupons) {
        this.freeCoupons = freeCoupons;
    }

    public Long getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(Long productQuantity) {
        this.productQuantity = productQuantity;
    }

    public Long getOfferApplied() {
        return offerApplied;
    }

    public void setOfferApplied(Long offerApplied) {
        this.offerApplied = offerApplied;
    }

    public Long getCouponsApplied() {
        return couponsApplied;
    }

    public void setCouponsApplied(Long couponsApplied) {
        this.couponsApplied = couponsApplied;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getCuttedPrice() {
        return cuttedPrice;
    }

    public void setCuttedPrice(String cuttedPrice) {
        this.cuttedPrice = cuttedPrice;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    //////cart item

    /////cart total
    private int totalItems, totalItemsPrice, totalAmount, savedAmount;
    private String deliveryPrice;

    public CartItemModel(int type) {
        this.type = type;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalItemsPrice() {
        return totalItemsPrice;
    }

    public void setTotalItemsPrice(int totalItemsPrice) {
        this.totalItemsPrice = totalItemsPrice;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getSavedAmount() {
        return savedAmount;
    }

    public void setSavedAmount(int savedAmount) {
        this.savedAmount = savedAmount;
    }

    public String getDeliveryPrice() {
        return deliveryPrice;
    }

    public void setDeliveryPrice(String deliveryPrice) {
        this.deliveryPrice = deliveryPrice;
    }

    /////cart total

}
