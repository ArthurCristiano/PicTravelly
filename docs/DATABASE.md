# 🗄️ Modelagem de Dados e Persistência

O aplicativo utiliza uma estratégia dupla: **Room Database** para dados estruturados (relacionais) e
**Preferences DataStore** para chave-valor (configurações do sistema).

## 1. Preferences DataStore (Settings)

Garante que as configurações sobrevivam ao fechamento do app.

* `APP_THEME`: String (LIGHT, DARK, SYSTEM)
* `APP_LANGUAGE`: String (pt-br, en-us, es-es)
* `MAP_TYPE`: Int (NORMAL, SATELLITE, TERRAIN)
* `MAP_DEFAULT_ZOOM`: Float

## 2. Room Database (Entidades)

```
    TOURIST_SPOT {
        Long id PK "autoGenerate"
        String title
        String description
        String locationName "Cidade ou endereço por extenso"
        Long visitDate "Timestamp epoch"
        Double latitude
        Double longitude
        String offlineMapSnapshotUri "URI da foto estática do mapa"
        Long createdAt
    }

    SPOT_IMAGE {
        Long id PK "autoGenerate"
        Long spotId FK "Cascade Delete"
        String imageUri "Caminho interno no Android"
        Boolean isCover "Define capa da listagem"
    }
```

## Notas Arquiteturais de Persistência:

- **Paginação:** O DAO da entidade TouristSpot deve retornar um PagingSource<Int, TouristSpotEntity>
  em vez de uma List simples.

- **Armazenamento de Imagem:** A imageUri aponta para o Internal Storage do app. Quando o usuário
  clica em "Salvar no celular", a imagem é copiada usando o MediaStore API para a pasta pública "
  Pictures" do Android.

- **Mapa Offline:** offlineMapSnapshotUri armazena a captura de tela gerada silenciosamente no
  momento da criação do diário, caso a internet falhe no futuro.