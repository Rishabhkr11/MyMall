package rishabh.example.mymall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private TextView textView;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchView = findViewById(R.id.search_view);
        textView = findViewById(R.id.textview);
        recyclerView = findViewById(R.id.recycler_view);

        recyclerView.setVisibility(View.VISIBLE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);

        final List<WishlistModel> list = new ArrayList<>();
        final List<String> ids = new ArrayList<>();

        final Adapter adapter = new Adapter(list, false);
        adapter.setFromSearch(true);
        recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                list.clear();
                ids.clear();

                final String[] tags = query.toLowerCase().split(" ");
                for (final String tag : tags) {
                    tag.trim();
                    FirebaseFirestore.getInstance().collection("PRODUCTS").whereArrayContains("tags", tag)
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                                    WishlistModel model = new WishlistModel(documentSnapshot.getId(), documentSnapshot.get("product_image_1").toString()
                                            , (long) documentSnapshot.get("free_coupons")
                                            , (long) documentSnapshot.get("total_ratings")
                                            , documentSnapshot.get("product_title").toString()
                                            , documentSnapshot.get("average_rating").toString()
                                            , documentSnapshot.get("product_price").toString()
                                            , documentSnapshot.get("cutted_price").toString()
                                            , (boolean) documentSnapshot.get("COD")
                                            , true);

                                    model.setTags((ArrayList<String>) documentSnapshot.get("tags"));

                                    if (!ids.contains(model.getProductID())) {
                                        list.add(model);
                                        ids.add(model.getProductID());
                                    }
                                }
                                if (tag.equals(tags[tags.length - 1])) {
                                    if (list.size() == 0) {
                                        textView.setVisibility(View.VISIBLE);
                                        recyclerView.setVisibility(View.GONE);
                                    } else {
                                        textView.setVisibility(View.GONE);
                                        recyclerView.setVisibility(View.VISIBLE);
                                        adapter.getFilter().filter(query);
                                    }
                                }
                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(SearchActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    class Adapter extends WishlistAdapter implements Filterable {

        private List<WishlistModel> originalList;

        public Adapter(List<WishlistModel> wishlistModelList, Boolean wishlist) {
            super(wishlistModelList, wishlist);
            originalList = wishlistModelList;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {

                    FilterResults results = new FilterResults();
                    List<WishlistModel> filteresList = new ArrayList<>();

                    final String[] tags = constraint.toString().toLowerCase().split(" ");

                    for (WishlistModel model : originalList){
                        ArrayList<String> presetTags = new ArrayList<>();
                        for (String tag : tags){
                            if (model.getTags().contains(tag)){
                                presetTags.add(tag);
                            }
                        }
                        model.setTags(presetTags);
                    }
                    for (int i = tags.length; i > 0; i--){
                        for (WishlistModel model : originalList){
                            if (model.getTags().size() == i){
                                filteresList.add(model);
                            }
                        }
                    }
                    results.values = filteresList;
                    results.count = filteresList.size();
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {

                    if (results.count > 0){
                        setWishlistModelList((List<WishlistModel>) results.values);
                    }
                    notifyDataSetChanged();
                }
            };
        }
    }
}