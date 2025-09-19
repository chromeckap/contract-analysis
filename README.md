
# Contract Analysis
... je nástroj pro analýzu smluvních dokumentů. Umožňuje nahrát smlouvu ve formátu PDF nebo DOCX a získat strukturovanou analýzu obsahu pomocí API. Tento projekt byl vytvořen pro mini-hackaton firmy [agrp.dev](https://agrp.dev).

## Environment Variables
Pro spuštění projektu je potřeba vytvořit soubor `.env` a nastavit následující proměnné:
```bash
OPEN_AI_API_KEY=value
```
## Laws
```
/
├──  src/
│    └── main/
│        └── java/
│        └── resources/
│            └── data/
│                └── laws/
│                    └── example_laws.json (<- zde přidejte zákony, neomezené množství souborů)
│                    └── example_laws2.json
```

* Zákony v json souborech **musí** být definovány takto:

```json
[
  {
    "name": "Občanský zákoník § 553",
    "requirement": "Smlouva musí obsahovat jasně určené smluvní strany."
  },
  {
    "name": "... název zákonu",
    "requirement": "... definice"
  }
]
```

## Deployment
Postup pro spuštění projektu:
```bash
# 1. Klonování repozitáře
gh repo clone chromeckap/contract-analysis

# 2. Přejděte do složky projektu
cd contract-analysis

# 3. Spuštění pomocí Docker Compose
docker compose up -d
```
Po úspěšném spuštění aplikace bude API dostupné na adrese http://localhost:8080

## API Reference

#### Analýza smlouvy
- Analyzuje smlouvu a vrací strukturovaný výstup.
```http
  POST /api/v1/contracts/analyze
```

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `file` | `MultipartFile` | Smlouva v podobě textového souboru (pdf, docx)  |

- Response
```json
{
    "issues": [
        {
            "passage": "Neúplnost smlouvy: Chybí zásadní informace o předmětu kupní smlouvy.",
            "recommendation": "Doplnit informace o předmětu prodeje v souladu s Občanským zákoníkem § 1746.",
            "importance": "HIGH"
        },
        {
            "passage": "... informace o chybě",
            "recommendation": "... typ k jejímu opravení odkazující na zákoník §",
            "importance": "HIGH"
        }
    ]
}
```

## Usage
1. Spusťte projekt podle instrukcí v sekci Deployment.
2. Použijte nástroj (např. Postman) pro odeslání souboru na endpoint http://localhost:8080/api/v1/contracts/analyze.
3. Získejte strukturovanou analýzu smlouvy.

## Použité zdroje
- [https://www.the-main-thread.com/p/chain-of-thought-java-langchain4j-quarkus](https://www.the-main-thread.com/p/chain-of-thought-java-langchain4j-quarkus)
- [https://docs.spring.io/spring-ai/reference/api/chatclient.html](https://docs.spring.io/spring-ai/reference/api/chatclient.html)
