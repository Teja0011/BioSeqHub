from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_read_root():
    response = client.get("/")
    assert response.status_code == 200
    assert response.json()["service"] == "BioSeq Research Hub API"

def test_ncbi_search():
    response = client.get("/api/v1/ncbi/search?term=TP53")
    assert response.status_code == 200
    data = response.json()
    assert data["source"] == "NCBI Entrez"
    assert len(data["results"]) > 0

def test_pubchem_compound():
    response = client.get("/api/v1/pubchem/compound?query=Aspirin")
    assert response.status_code == 200
    assert response.json()["cid"] == 2244

def test_ai_chat():
    response = client.post("/api/v1/ai/chat", json={"prompt": "Explain p53 function", "accession": "P04637"})
    assert response.status_code == 200
    assert response.json()["is_ai_generated"] is True
