<?php
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    include_once '../racine.php';
    include_once RACINE . '/service/EtudiantService.php';
    loadAll();
}
function loadAll() {
    try {
        $es = new EtudiantService();
        $data = $es->findAllApi();
        header('Content-type: application/json');
        echo json_encode($data);
        http_response_code(200); // OK
    } catch (Exception $e) {
        // En cas d'erreur
        http_response_code(500); // Internal Server Error
        echo json_encode(['error' => 'Something went wrong', 'details' => $e->getMessage()]);
    }
}
