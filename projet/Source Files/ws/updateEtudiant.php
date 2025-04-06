<?php
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    include_once '../racine.php';
    include_once RACINE.'/service/EtudiantService.php';
    update();
}

function update() {
    extract($_POST);
    print_r($_POST);  // Debugging line to check incoming POST data
    print_r("Root: " . $_SERVER['DOCUMENT_ROOT']);  // Print root path for debugging

    // Check if necessary fields are set
    if (isset($id, $nom, $prenom, $ville, $sexe)) {

        // Handle image upload (if provided)
        $imagePath = null;
        if (isset($_POST['image']) && !empty($_POST['image'])) {
            $base64Image = $_POST['image'];
            $es = new EtudiantService();
            $etudiant = $es->findById($id);
            $oldImagePath = $etudiant->getImage();

            // Remove old image if it exists
            if ($oldImagePath && file_exists($_SERVER['DOCUMENT_ROOT'] . $oldImagePath)) {
                unlink($_SERVER['DOCUMENT_ROOT'] . $oldImagePath);
            }

            // Get the image data and extension
            $imageParts = explode(',', $base64Image);
            $imageData = base64_decode(end($imageParts));  // In case "data:image/png;base64,..." format

            $imageExtension = "png"; // default, or detect from base64 header if needed
            if (strpos($imageParts[0], 'jpeg') !== false) {
                $imageExtension = "jpg";
            } elseif (strpos($imageParts[0], 'gif') !== false) {
                $imageExtension = "gif";
            }

            $imageName = uniqid('img_', true) . "." . $imageExtension;
            $imagePath = "/uploads/" . $imageName;
            $destination = $_SERVER['DOCUMENT_ROOT'] . "/projet/Source Files" . $imagePath;

            if (file_put_contents($destination, $imageData)) {
                echo "Image uploaded from base64.";
            } else {
                echo "Failed to save image from base64.";
                $imagePath = null;
            }
        } else {
            // If no new image is provided, keep the old image
            $es = new EtudiantService();
            $etudiant = $es->findById($id);
            $imagePath = $etudiant->getImage();  // Retain the existing image
        }

        // Check if a new date of birth is provided
        if (!isset($dateNaissance) || empty($dateNaissance)) {
            $dateNaissance = null;  // If no date is provided, set it to null
        }

        // Create a new Etudiant object with the updated data
        $etudiant = new Etudiant($id, $nom, $prenom, $ville, $sexe, $dateNaissance, $imagePath);

        // Call the update function to update the student in the database
        $es = new EtudiantService();
        $es->update($etudiant);

        // Return the updated list of students as a JSON response
        header('Content-Type: application/json');
        echo json_encode($es->findAllApi());
    } else {
        // If some required fields are missing, send an error message
        echo json_encode(["error" => "Données incomplètes"]);
    }
}
