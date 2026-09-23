# ✈️ PicTravelly

PicTravelly é um aplicativo Android "offline-first" que funciona como um **diário de viagens**. Cada
viagem é um card na tela inicial; dentro dela ficam os pontos turísticos visitados, com geocódigos,
nome, descrição, endereço e foto.

Trabalho da disciplina de Desenvolvimento para Dispositivos Móveis.

## ✅ Requisitos do trabalho e onde eles estão

| Requisito                                  | Implementação                                                                          |
|--------------------------------------------|----------------------------------------------------------------------------------------|
| CRUD de pontos turísticos                  | `feature/ponto/TelaEditorPonto` + `data/repositorio/RepositorioDoDiario`                 |
| Geocódigos, nome, descrição e imagem       | `data/local/entidade/Ponto`                                                              |
| Persistência local                         | Room (SQLite) em `core/banco/BancoDeDados`                                               |
| Geocódigo → endereço textual               | `core/localizacao/ServicoDeGeocodificacao` (API `android.location.Geocoder`)             |
| Pontos em um Google Maps                   | `feature/mapa/TelaMapa` (Maps Compose)                                                   |
| Zoom padrão e tipo de mapa                 | `feature/configuracoes/TelaConfiguracoes` + `core/configuracoes/RepositorioDeConfiguracoes` |

O endereço resolvido é **gravado junto do ponto** no banco, então ele continua visível depois, mesmo
sem conexão.

## 🛠️ Stack

* **Linguagem:** Kotlin 2.3.10
* **UI:** Jetpack Compose (Material Design 3)
* **Navegação:** Navigation Compose
* **Persistência:** Room 2.8.5 via KSP + DataStore Preferences (configurações e perfil)
* **Imagens:** Coil, com os arquivos no armazenamento interno do app
* **Mapas:** Maps Compose + Play Services Maps
* **Localização:** `LocationManager` nativo (sem depender do Play Services)
* **SDK:** compile 37, min 29

> **⚠️ Injeção de dependências:**
> O Hilt foi ejetado por instabilidade de compilação com o KSP do Room (Issue #3965). O projeto usa
> **DI manual** via `core/di/ContainerDoApp` e `core/di/ProvedorDeViewModels`. Não reintroduza
> Dagger/Hilt sem provas de conceito isoladas de compilação.

## 🔑 Configuração obrigatória: chave do Google Maps

A chave **não é versionada**. Sem ela o app compila e roda, mas o mapa aparece em branco.

1. Gere uma chave no [Google Cloud Console](https://console.cloud.google.com/) com a
   *Maps SDK for Android* habilitada.
2. Abra o arquivo `local.properties` na raiz do projeto (ele é ignorado pelo Git).
3. Preencha a linha:

```properties
MAPS_API_KEY=SUA_CHAVE_AQUI
```

4. Faça o *Gradle Sync*. O `app/build.gradle.kts` injeta o valor no manifesto via
   `manifestPlaceholders`.

## 📐 Estrutura de pastas

O repositório adota **Package by Feature** com os preceitos de Clean Architecture. A regra é coesão:
tudo o que uma tela precisa deve estar próximo a ela.

```text
com.app.pictravelly/
│
├── core/                       # Infraestrutura, sem regra de negócio de tela
│   ├── banco/                  # BancoDeDados (Room)
│   ├── configuracoes/          # Preferências do mapa e do perfil (DataStore)
│   ├── design/                 # Design system
│   │   ├── componentes/        # OBRIGATÓRIO usar daqui (PicTravellyBotao, PicTravellyCartao, ...)
│   │   └── tema/               # Cores (Oceano/Terracota) e tipografia híbrida
│   ├── di/                     # ContainerDoApp e ProvedorDeViewModels (DI manual)
│   ├── localizacao/            # ProvedorDeLocalizacao (GPS) e ServicoDeGeocodificacao
│   ├── midia/                  # ArmazenamentoDeImagens: fotos no armazenamento interno
│   ├── navegacao/              # Destino (abas) e Rotas
│   └── ui/                     # PicTravellyTelaPrincipal (Scaffold + NavHost) e utilitários
│
├── data/
│   ├── local/                  # entidade/, dao/, relacao/
│   └── repositorio/            # RepositorioDoDiario: banco + arquivos + geocodificação
│
├── feature/
│   ├── inicio/                 # Cards das viagens
│   ├── viagem/                 # Detalhe e formulário de viagem
│   ├── ponto/                  # Formulário do ponto turístico (CRUD)
│   ├── mapa/                   # Google Maps com todos os pontos
│   ├── perfil/                 # Perfil do viajante
│   └── configuracoes/          # Zoom padrão e tipo de mapa
│
└── PicTravellyApp.kt           # Application (inicializa o ContainerDoApp)
```

## 🧭 Navegação

O rodapé flutuante tem quatro abas — **Início, Mapa, Perfil, Ajustes** — e o botão **+** no centro,
que abre um menu para criar uma nova viagem ou um novo ponto turístico.

As telas de detalhe e os formulários abrem sem o rodapé, com uma `TopAppBar` e botão de voltar.

## 🎨 Design System e padrões de UI

Nunca utilize componentes brutos do Material Design diretamente nas telas quando houver um *wrapper*
em `core/design/componentes`.

* **Superfícies:** navegação Edge-to-Edge nativa. Não aplique fundos rígidos em `Scaffolds`. Respeite
  o `paddingValues` injetado e use espaçamento de rolagem para as listas passarem por trás da barra
  flutuante.
* **Barra de navegação:** o estado vem de `currentBackStackEntryAsState()`. Não use variáveis soltas
  para forçar a aba atual.
* **Tipografia:** híbrida. Títulos serifados (`headlineLarge`) para o tom de "diário clássico";
  descrições e interface em fonte sem serifa (`bodyLarge`). Resolvido ao usar `PicTravellyTitulo` e
  `PicTravellyTexto`.

## 🔤 Convenção de nomes

O código é escrito em **português**: arquivos, pacotes, classes, funções e propriedades.

Ficam em inglês apenas os nomes que o framework exige ou que são convenção universal do Android:

* `MainActivity` e `PicTravellyApp` (registrados no `AndroidManifest.xml`);
* sufixos de arquitetura: `ViewModel`, `Dao`;
* anotações do Room (`@Entity`, `@Dao`, `@Query`, ...);
* parâmetros padrão do Compose: `modifier`, `content`, `onClick`, `text`, `enabled`,
  `contentPadding`, `containerColor`, `contentColor`;
* `savedStateHandle` e as APIs do Navigation (`navController`, `startDestination`).

Callbacks seguem o idioma do Compose com verbo em português: o parâmetro usa o prefixo `on`
(`onAbrirViagem`, `onVoltar`, `onNavegarPara`) e o método do ViewModel que o atende usa `ao`
(`aoMudarTitulo`, `aoCapturarFoto`).

## 🚀 Como executar

1. Clone o repositório.
2. Abra na versão mais recente do Android Studio e aguarde o **Gradle Sync**.
3. Configure a `MAPS_API_KEY` (seção acima).
4. Use um emulador ou aparelho com Android 10 (API 29) ou superior.
5. Execute o módulo `app`.

### Permissões pedidas em tempo de execução

* **Câmera** — ao tocar em "Câmera" no formulário de foto.
* **Localização** — ao tocar em "Usar GPS" no cadastro do ponto turístico.

Ambas são opcionais: dá para digitar os geocódigos na mão e escolher a foto pela galeria.

### Testando o GPS no emulador

Três pontinhos → *Extended Controls* → *Location* → defina latitude e longitude (por exemplo,
`-25.4284` e `-49.2733`, Curitiba) e clique em *Set Location*.

### Caminho do projeto com acento

O `gradle.properties` traz `android.overridePathCheck=true` porque o projeto vive em uma pasta com
acento ("Área de Trabalho"). Se aparecer algum erro estranho de build no Windows, mover o projeto
para um caminho só com ASCII resolve.
