package com.example.projetws;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.projetws.adapter.EtudiantAdapter;
import com.example.projetws.beans.Etudiant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListEtudiantsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EtudiantAdapter adapter;
    private RequestQueue requestQueue;
    private static final String URL_LIST = "http://192.168.56.1/projet/Source Files/ws/loadEtudiant.php";
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int SELECT_IMAGE_REQUEST_CODE = 2;

    private Uri newImageUri;
    private ImageView imageView;
    private Button ajouterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_etudiants);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestQueue = Volley.newRequestQueue(this);

        fetchEtudiants(); // Fetch the list of students

        ajouterBtn = findViewById(R.id.ajouterBtn);
        ajouterBtn.setOnClickListener(v -> startActivity(new Intent(ListEtudiantsActivity.this, AddEtudiantActivity.class)));
    }

    private void fetchEtudiants() {
        StringRequest request = new StringRequest(Request.Method.POST, URL_LIST,
                response -> {
                    List<Etudiant> etudiants = new Gson().fromJson(response, new TypeToken<List<Etudiant>>() {
                    }.getType());
                    adapter = new EtudiantAdapter(this, etudiants, new EtudiantAdapter.OnEtudiantClickListener() {
                        @Override
                        public void onEdit(Etudiant etudiant) {
                            showEditDialog(etudiant);
                        }

                        @Override
                        public void onDelete(Etudiant etudiant) {
                            confirmDelete(etudiant);
                        }
                    });
                    recyclerView.setAdapter(adapter);
                },
                error -> Toast.makeText(this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show()
        );
        requestQueue.add(request);
    }

    private void confirmDelete(Etudiant etudiant) {
        new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this student?")
                .setPositiveButton("Yes", (dialog, which) -> deleteEtudiant(etudiant.getId()))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteEtudiant(int id) {
        StringRequest request = new StringRequest(Request.Method.POST, "http://192.168.56.1/projet/Source Files/ws/deleteEtudiant.php",
                response -> fetchEtudiants(), // Refresh list after deletion
                error -> Toast.makeText(this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(id));
                return params;
            }
        };
        fetchEtudiants();
        requestQueue.add(request);
    }

    private void showEditDialog(final Etudiant etudiant) {
        newImageUri = null;  // Clear any existing image URI.
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.item_edit_etudiant, null);

        final EditText etNom = dialogView.findViewById(R.id.et_nom);
        final EditText etPrenom = dialogView.findViewById(R.id.et_prenom);
        final Spinner spinnerVille = dialogView.findViewById(R.id.spinner_ville);
        final RadioGroup radioGroupSexe = dialogView.findViewById(R.id.radio_group_sexe);
        final Button btnSave = dialogView.findViewById(R.id.btn_save);
        final Button btnDatePicker = dialogView.findViewById(R.id.date_picker_button);
        imageView = dialogView.findViewById(R.id.imageView);
        final Button btnCaptureImage = dialogView.findViewById(R.id.capture_image_button);

        // Set initial data for the student to edit.
        etNom.setText(etudiant.getNom());
        etPrenom.setText(etudiant.getPrenom());
        String currentImageUrl = "http://192.168.56.1/projet/Source%20Files" + etudiant.getImagePath();
        Glide.with(this).load(currentImageUrl).into(imageView);
        imageView.setTag(etudiant.getImagePath());  // Set image tag with the current image path
        btnDatePicker.setText(etudiant.getDateNaissance());

        // Camera capture action
        btnCaptureImage.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
        });

        // Date Picker logic
        btnDatePicker.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(ListEtudiantsActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        String formattedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);
                        btnDatePicker.setText(formattedDate);

                    },
                    2000, 0, 1);  // Default date (year 2000, January 1st)
            datePickerDialog.show();
        });

        // Set the radio button for the sexe
        if (etudiant.getSexe().equals("homme")) {
            radioGroupSexe.check(R.id.radio_homme); // Homme is selected
        } else {
            radioGroupSexe.check(R.id.radio_femme); // Femme is selected
        }

        // Set the spinner for the ville
        ArrayAdapter<String> spinnerAdapter = (ArrayAdapter<String>) spinnerVille.getAdapter();
        int villePosition = spinnerAdapter.getPosition(etudiant.getVille());
        spinnerVille.setSelection(villePosition);

        // Set up the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Save button logic.
        btnSave.setOnClickListener(v -> {
            // Retrieve updated information from dialog.
            String updatedNom = etNom.getText().toString();
            String updatedPrenom = etPrenom.getText().toString();
            String updatedVille = spinnerVille.getSelectedItem().toString();
            String updatedSexe = (radioGroupSexe.getCheckedRadioButtonId() == R.id.radio_homme) ? "homme" : "femme";
            String updatedDate = btnDatePicker.getText().toString();

            // Check if the imageView has a drawable (image) set before getting the bitmap.
            String base64Image = "";
            if (imageView.getDrawable() != null) {
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                base64Image = encodeImageToBase64(bitmap);
            } else {
                // Handle the case where no image is set. You can either leave it as an empty string,
                // use a default image, or set a flag to indicate no image is provided.
                Log.d("Save", "No image selected");
            }

            Etudiant updatedEtudiant = new Etudiant(etudiant.getId(), updatedNom, updatedPrenom, updatedVille, updatedSexe, updatedDate, base64Image);

            updateEtudiant(updatedEtudiant);  // Call the method to update the etudiant.

            fetchEtudiants();
            dialog.dismiss();  // Close the dialog.
        });
    }



    private void updateEtudiant(Etudiant updatedEtudiant) {
        StringRequest request = new StringRequest(Request.Method.POST, "http://192.168.56.1/projet/Source Files/ws/updateEtudiant.php",
                response -> {
                    Log.d("Response", response);
                    Toast.makeText(ListEtudiantsActivity.this, "Étudiant mis à jour avec succès", Toast.LENGTH_SHORT).show();
                    fetchEtudiants();
                },
                error -> {
                    Log.e("Error", error.toString());
                    Toast.makeText(ListEtudiantsActivity.this, "Erreur de mise à jour", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(updatedEtudiant.getId()));
                params.put("nom", updatedEtudiant.getNom());
                params.put("prenom", updatedEtudiant.getPrenom());
                params.put("ville", updatedEtudiant.getVille());
                params.put("sexe", updatedEtudiant.getSexe());
                params.put("dateNaissance", updatedEtudiant.getDateNaissance());

                // Ici on encode en base64 directement à partir de l'imageView
                String base64Image = "";
                if (imageView.getDrawable() != null) {
                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    base64Image = encodeImageToBase64(bitmap);
                }

                params.put("image", base64Image);

                return params;
            }
        };
        requestQueue.add(request);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                // Handle camera capture result.
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(photo);

                // Convert the image to Base64
                String base64Image = encodeImageToBase64(photo);
                newImageUri = saveImageToCache(photo);  // Optional: You can also save the image to cache if needed
            } else if (requestCode == SELECT_IMAGE_REQUEST_CODE) {
                // Handle gallery selection.
                Uri selectedImageUri = data.getData();
                imageView.setImageURI(selectedImageUri);
                newImageUri = selectedImageUri;
            }
        }
    }

    // Convert Bitmap image to Base64
    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Uri saveImageToCache(Bitmap bitmap) {
        File cacheDir = getCacheDir();
        File tempFile = new File(cacheDir, "temp_image.png");
        try {
            FileOutputStream outStream = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
            return Uri.fromFile(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchEtudiants();
    }
}
