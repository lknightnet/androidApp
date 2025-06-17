package com.application.restaurantv2;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ProductListItem> itemList;
    private Context context;
    private OnProductClickListener listener;

    public CatalogAdapter(Context context, List<ProductListItem> itemList, OnProductClickListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ProductListItem.TYPE_CATEGORY) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_category_title, parent, false);
            return new CategoryViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
            return new ProductViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CategoryViewHolder) {
            CategoryTitleItem categoryItem = (CategoryTitleItem) itemList.get(position);
            ((CategoryViewHolder) holder).title.setText(categoryItem.getCategoryName());

        } else {
            ProductSmall product = (ProductSmall) itemList.get(position);
            ProductViewHolder h = (ProductViewHolder) holder;
            h.productName.setText(product.getName());
            h.productButtonBuy.setText(product.getPrice() + " ₽");
            h.productWeight.setText(product.getWeight());
            Glide.with(context).load(product.getImageUrl()).circleCrop().into(h.productImage);
            holder.itemView.setOnClickListener(v -> listener.onProductClick(product));

            h.productButtonBuy.setOnClickListener(v -> {
                if (context instanceof ProductActivity) {
                    ((ProductActivity) context).sendAddToCartRequest(product.getId());
                }
            });

        }
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        CategoryViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.categoryTitle);
        }
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productWeight;
        Button productButtonBuy;

        ProductViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productWeight = itemView.findViewById(R.id.productWeight);
            productButtonBuy = itemView.findViewById(R.id.productButtonBuy);

        }
    }

    public void updateItems(List<ProductListItem> newItems) {
        this.itemList = newItems;
        notifyDataSetChanged();
    }

}
