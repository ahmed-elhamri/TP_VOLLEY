package com.example.projetws.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projetws.R;
import com.example.projetws.beans.Etudiant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EtudiantAdapter extends RecyclerView.Adapter<EtudiantAdapter.EtudiantViewHolder> {
    private Context context;
    private List<Etudiant> etudiants;
    private OnEtudiantClickListener listener;

    public interface OnEtudiantClickListener {
        void onEdit(Etudiant etudiant);
        void onDelete(Etudiant etudiant);
    }

    public EtudiantAdapter(Context context, List<Etudiant> etudiants, OnEtudiantClickListener listener) {
        this.context = context;
        this.etudiants = etudiants;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EtudiantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_etudiant, parent, false);
        return new EtudiantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EtudiantViewHolder holder, int position) {
        Etudiant etudiant = etudiants.get(position);
        holder.nomPrenom.setText(etudiant.getNom() + " " + etudiant.getPrenom());
        holder.ville.setText(etudiant.getVille());

        // Format the Date de Naissance
        String formattedDate = formatDate(etudiant.getDateNaissance());
        holder.dateNaissance.setText(formattedDate);

        // Image loading check
        String imagePath = etudiant.getImagePath();
            Log.d("Image URL", "Loading image from URL: " + imagePath);
        if (imagePath != null && !imagePath.isEmpty()) {
            // Construct the full URL for the image
            String imageUrl = "http://192.168.56.1/projet/Source%20Files" + imagePath;

            // Load image with Glide
            Glide.with(context)
                    .load(imageUrl) // Use the full URL here
                    .placeholder(R.drawable.ic_launcher_foreground) // default placeholder
                    .into(holder.imageView);
        } else {
            Glide.with(context)
                    .load(R.drawable.ic_launcher_foreground)  // fallback image if null or empty
                    .into(holder.imageView);
        }

        holder.itemView.setOnClickListener(v -> showPopup(etudiant));
    }


    private String formatDate(String date) {
        if (date == null || date.isEmpty()) {
            return "";  // Return empty string or a default value when date is null/empty
        }

        // Convert and format the date (assuming the backend provides it in yyyy-MM-dd format)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        try {
            Date parsedDate = sdf.parse(date);
            return outputFormat.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return date;  // Return original if parsing fails
        }
    }

    @Override
    public int getItemCount() {
        return etudiants.size();
    }

    public class EtudiantViewHolder extends RecyclerView.ViewHolder {
        TextView nomPrenom, ville, dateNaissance;
        ImageView imageView;  // Add ImageView to display student image

        public EtudiantViewHolder(@NonNull View itemView) {
            super(itemView);
            nomPrenom = itemView.findViewById(R.id.nomPrenom);
            ville = itemView.findViewById(R.id.ville);
            dateNaissance = itemView.findViewById(R.id.dateNaissance);
            imageView = itemView.findViewById(R.id.imageView);  // Bind ImageView
        }
    }

    private void showPopup(Etudiant etudiant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choisir une action")
                .setItems(new CharSequence[]{"Modifier", "Supprimer"}, (dialog, which) -> {
                    if (which == 0) {
                        listener.onEdit(etudiant);
                    } else {
                        listener.onDelete(etudiant);
                    }
                })
                .show();
    }
}
