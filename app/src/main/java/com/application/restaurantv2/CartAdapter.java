package com.application.restaurantv2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.application.restaurantv2.R;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_EXTRA_CARD = 1;

    private final Context context;
    private final List<CartItem> cartItems;

    public CartAdapter(Context context, List<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @Override
    public int getItemCount() {
        return cartItems.isEmpty() ? 0 : cartItems.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (!cartItems.isEmpty() && position == cartItems.size()) {
            return TYPE_EXTRA_CARD;
        } else {
            return TYPE_ITEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_EXTRA_CARD) {
            View view = inflater.inflate(R.layout.item_fixed_card, parent, false);
            return new ExtraCardViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_cart, parent, false);
            return new CartViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_EXTRA_CARD) {
            ((ExtraCardViewHolder) holder).bind();
        } else {
            CartItem item = cartItems.get(position);
            CartViewHolder h = (CartViewHolder) holder;

            h.itemName.setText(item.getName());
            h.itemWeight.setText(item.getWeight());
            h.itemPrice.setText(item.getPrice() + " ₽");
            h.itemQuantity.setText(String.valueOf(item.getQuantity()));
            Glide.with(context).load(item.getImageUrl()).into(h.itemImage);

            h.btnPlus.setOnClickListener(v -> {
                if (context instanceof CartActivity) {
                    ((CartActivity) context).sendAddToCartRequest(item.getId());
                }
            });

            h.btnMinus.setOnClickListener(v -> {
                if (context instanceof CartActivity) {
                    ((CartActivity) context).sendRemoveToCartRequest(item.getId());
                }
            });
        }
    }

    public List<CartItem> getItems() {
        return cartItems;
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName, itemWeight, itemPrice, itemQuantity;
        Button btnPlus, btnMinus;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemName = itemView.findViewById(R.id.itemName);
            itemWeight = itemView.findViewById(R.id.itemWeight);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemQuantity = itemView.findViewById(R.id.itemQuantity);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
        }
    }

    public static class ExtraCardViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemQuantity;
        Button btnPlus, btnMinus;

        public ExtraCardViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.fixedItemName);
            itemQuantity = itemView.findViewById(R.id.fixedItemQuantity);
            btnPlus = itemView.findViewById(R.id.fixedBtnPlus);
            btnMinus = itemView.findViewById(R.id.fixedBtnMinus);
        }

        public void bind() {
            itemQuantity.setText("1");

            btnPlus.setOnClickListener(v -> {
                int qty = Integer.parseInt(itemQuantity.getText().toString());
                qty++;
                itemQuantity.setText(String.valueOf(qty));

                if (itemView.getContext() instanceof CartActivity) {
                    ((CartActivity) itemView.getContext()).updateFixedItemQuantity(qty);
                }
            });

            btnMinus.setOnClickListener(v -> {
                int qty = Integer.parseInt(itemQuantity.getText().toString());
                if (qty > 1) {
                    qty--;
                    itemQuantity.setText(String.valueOf(qty));

                    if (itemView.getContext() instanceof CartActivity) {
                        ((CartActivity) itemView.getContext()).updateFixedItemQuantity(qty);
                    }
                }
            });
        }
    }
}
