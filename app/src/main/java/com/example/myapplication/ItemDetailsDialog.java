package com.example.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

public class ItemDetailsDialog extends Dialog {

    public ItemDetailsDialog(Context context, ItemDetail itemDetail) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_item_details);

        setupDialogWidth();
        initializeViews(itemDetail);

        Button btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());
    }

    private void setupDialogWidth() {
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            DisplayMetrics displayMetrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                windowManager.getDefaultDisplay().getMetrics(displayMetrics);

                int screenWidth = displayMetrics.widthPixels;
                int dialogWidth = (int) (screenWidth * 0.90); // Slightly wider for better content display
                int dialogHeight = (int) (displayMetrics.heightPixels * 0.80); // Set max height

                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(window.getAttributes());
                layoutParams.width = dialogWidth;
                layoutParams.height = dialogHeight;
                layoutParams.gravity = android.view.Gravity.CENTER;

                window.setAttributes(layoutParams);
            }
        }
    }

    private void initializeViews(ItemDetail itemDetail) {
        TextView tvItemName = findViewById(R.id.tvItemName);
        TextView tvItemBrand = findViewById(R.id.tvItemBrand);
        TextView tvItemPrice = findViewById(R.id.tvItemPrice);
        TextView tvPurchaseDate = findViewById(R.id.tvPurchaseDate);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvItemLinks = findViewById(R.id.tvItemLinks);
        TextView tvItemDoc = findViewById(R.id.tvItemDoc);
        TextView tvRemarks = findViewById(R.id.tvRemarks);
        ImageView ivItemImage = findViewById(R.id.ivItemImage);

        // Set item name with larger text
        if (tvItemName != null) {
            tvItemName.setText(itemDetail.getItemName() != null ? itemDetail.getItemName() : "N/A");
        }

        // Set brand
        if (tvItemBrand != null) {
            tvItemBrand.setText("Brand: " + (itemDetail.getItemBrand() != null ? itemDetail.getItemBrand() : "N/A"));
        }

        // Set price with proper formatting
        if (tvItemPrice != null) {
            tvItemPrice.setText(String.format("Price: $%.2f", itemDetail.getItemPrice()));
        }

        // Set formatted purchase date
        if (tvPurchaseDate != null) {
            tvPurchaseDate.setText("Purchased: " + itemDetail.getFormattedDateTime());
        }

        // Set description with proper formatting
        if (tvDescription != null) {
            String description = itemDetail.getDescription() != null ? itemDetail.getDescription() : "No description available";
            tvDescription.setText(description);
        }

        // Set item links with clickable URLs
        if (tvItemLinks != null) {
            String links = itemDetail.getItemLinks() != null ? itemDetail.getItemLinks() : "No links available";
            tvItemLinks.setText(links);
            Linkify.addLinks(tvItemLinks, Linkify.WEB_URLS);
            tvItemLinks.setLinksClickable(true);
        }

        // Set documentation links with clickable URLs
        if (tvItemDoc != null) {
            String doc = itemDetail.getItemDoc() != null ? itemDetail.getItemDoc() : "No documentation available";
            tvItemDoc.setText(doc);
            Linkify.addLinks(tvItemDoc, Linkify.WEB_URLS);
            tvItemDoc.setLinksClickable(true);
        }

        // Set remarks
        if (tvRemarks != null) {
            String remarks = itemDetail.getRemarks() != null ? itemDetail.getRemarks() : "No remarks";
            tvRemarks.setText(remarks);
        }

        // Load image using Glide with error handling
        if (ivItemImage != null) {
            loadItemImage(itemDetail, ivItemImage);
        }
    }

    private void loadItemImage(ItemDetail itemDetail, ImageView imageView) {
        String imageUrl = itemDetail.getImageUrl();

        // Check if we have a base64 image first
        if (itemDetail.getCapturedImageBase64() != null && !itemDetail.getCapturedImageBase64().isEmpty()) {
            // Load base64 image
            try {
                // You would need to implement base64 image loading here
                // For now, fall back to default image
                imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            } catch (Exception e) {
                imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
        // Check if we have a local file path
        else if (imageUrl != null && (imageUrl.startsWith("/") || imageUrl.startsWith("file://"))) {
            // Load local file
            try {
                Glide.with(getContext())
                        .load(imageUrl.startsWith("file://") ? imageUrl : new java.io.File(imageUrl))
                        .apply(new RequestOptions()
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_report_image)
                                .transform(new RoundedCorners(16)))
                        .into(imageView);
            } catch (Exception e) {
                imageView.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        }
        // Check if we have a web URL
        else if (imageUrl != null && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
            // Load web image
            Glide.with(getContext())
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_report_image)
                            .transform(new RoundedCorners(16)))
                    .into(imageView);
        }
        // Default image
        else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    // Overloaded constructor for backward compatibility
    public ItemDetailsDialog(Context context, ItemDetail itemDetail, Object listener) {
        this(context, itemDetail);
    }
}