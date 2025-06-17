package com.application.restaurantv2;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final Context context;
    private final List<OrderSmall> orderSmallList;

    public OrdersAdapter(Context context, List<OrderSmall> orderSmallList) {
        this.context = context;
        this.orderSmallList = orderSmallList;
    }

    @Override
    public int getItemCount() {
        return orderSmallList.size();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        OrderSmall order = orderSmallList.get(position);
        OrdersAdapter.OrderViewHolder h = (OrdersAdapter.OrderViewHolder) holder;
        h.orderNumber.setText("#" + order.getId());
        h.orderStatus.setText(order.getStatus());

        h.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, OrderInformationActivity.class);
                intent.putExtra("order_id", order.getId());
                context.startActivity(intent);
        });
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView orderNumber;
        TextView orderStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderNumber = itemView.findViewById(R.id.orderNumber);
            orderStatus = itemView.findViewById(R.id.orderStatus);
        }
    }
}
