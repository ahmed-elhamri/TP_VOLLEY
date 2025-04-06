<?php
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    include_once '../racine.php';
    include_once RACINE.'/service/EtudiantService.php';
    delete();
}

function delete() {
    extract($_POST);
    print_r($_POST);
        $es = new EtudiantService();
        $etudiant = $es->findById($id);
        $es->delete($etudiant);
        header('Content-Type: application/json');
        echo json_encode($es->findAllApi());

}
