<?php
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    include_once '../racine.php';
    include_once RACINE.'/service/EtudiantService.php';
    create();
}

function create() {
    extract($_POST);

    // Handle image as Base64 string instead of file
    $imagePath = null;
    if (isset($_POST['image']) && !empty($_POST['image'])) {
        $base64Image = $_POST['image'];

        // Clean the Base64 string if it contains the data URI prefix
        if (strpos($base64Image, "data:image") === 0) {
            $base64Image = explode(",", $base64Image)[1];
        }

        // Decode the image
        $imageData = base64_decode($base64Image);
        if ($imageData !== false) {
            // Create a unique image filename
            $imageName = uniqid('img_', true) . '.jpg';
            $uploadsDir = $_SERVER['DOCUMENT_ROOT'] . "/projet/Source Files/uploads/";
            $imageFilePath = $uploadsDir . $imageName;

            // Save image to file
            if (file_put_contents($imageFilePath, $imageData)) {
                $imagePath = '/uploads/' . $imageName;
                echo "Image saved successfully.\n";
            } else {
                echo "Failed to save image.\n";
            }
        } else {
            echo "Base64 decode failed.\n";
        }
    }

    // Create and save the Etudiant
    $es = new EtudiantService();
    $es->create(new Etudiant(1, $nom, $prenom, $ville, $sexe, $dateNaissance, $imagePath));

    // Return the list
    header('Content-type: application/json');
    echo json_encode($es->findAllApi());
}
