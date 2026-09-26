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
    TRIP {
        Long id PK "autoGenerate"
        String title
        String description
        Long startDate "Timestamp epoch"
        Long endDate "Nulo enquanto a viagem esta em andamento"
        String coverImageUri "Capa opcional do card"
        Long createdAt
    }

    TOURIST_SPOT {
        Long id PK "autoGenerate"
        Long tripId FK "Opcional, Set Null ao apagar a viagem"
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

- **Agrupamento por Viagem:** a entidade TRIP é o card exibido na aba Diário. O vínculo
  `TOURIST_SPOT.tripId` é **opcional**: apagar a viagem não apaga os pontos (SET_NULL), eles passam
  para o grupo "Pontos sem viagem". A consulta `TripWithSpots` usa relação em dois níveis (viagem →
  pontos → imagens), resolvendo a listagem e o detalhe em uma única leitura.

- **Versão do banco:** a inclusão da TRIP subiu o schema para a versão 2. O builder usa
  `fallbackToDestructiveMigration(dropAllTables = true)`, então instalações anteriores têm os dados
  recriados do zero em vez de migrados.

- **Paginação:** O DAO da entidade TouristSpot deve retornar um PagingSource<Int, TouristSpotEntity>
  em vez de uma List simples.

- **Armazenamento de Imagem:** A imageUri aponta para o Internal Storage do app. Quando o usuário
  clica em "Salvar no celular", a imagem é copiada usando o MediaStore API para a pasta pública "
  Pictures" do Android.

- **Mapa Offline:** offlineMapSnapshotUri armazena a captura de tela gerada silenciosamente no
  momento da criação do diário, caso a internet falhe no futuro.