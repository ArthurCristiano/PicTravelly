# ✈️ PicTravelly

PicTravelly é um aplicativo Android "offline-first" desenvolvido para atuar como um diário de pontos
turísticos. O objetivo é permitir o cadastro, gerenciamento e visualização de locais visitados,
focado em uma experiência imersiva e performática.

## 🛠️ Stack Tecnológico

O projeto foi inicializado utilizando o que há de mais moderno e estável no ecossistema Android (
Base 2026):

* **Linguagem:** Kotlin 2.4.20
* **UI:** Jetpack Compose (Material Design 3)
* **Navegação:** Navigation Compose (Type-Safe / State-Driven)
* **Persistência:** Room Database (2.8.5) via KSP
* **Gestão de Imagens:** Coil
* **SDK:** Compilado para SDK 36 (Min 29)

> **⚠️ Aviso Importante sobre Injeção de Dependências:**
> Para evitar instabilidades severas de compilação entre o KSP do Room e o Hilt (Issue #3965), **o
Hilt foi ejetado do projeto nesta etapa inicial**. O projeto utiliza **Manual Dependency Injection**
> via `AppContainer` e `ViewModelFactory`. Não reintroduza bibliotecas do Dagger/Hilt sem antes
> realizar provas de conceito isoladas de compilação.

## 📐 Arquitetura e Estrutura de Pastas

O repositório adota a estrutura **Package by Feature** combinada com os preceitos de Clean
Architecture. A regra é coesão: tudo o que uma tela precisa deve estar próximo a ela.

```text
com.app.pictravelly/
│
├── core/                   # O coração do aplicativo (Não contém regras de negócio de telas)
│   ├── designsystem/       # Componentes visuais base, paleta de cores e tipografia
│   │   ├── components/     # OBRIGATÓRIO: Use os componentes daqui (PicTravellyButton, PicTravellyCard, etc)
│   │   └── theme/          # Definições de MaterialTheme (Oceano/Terracota, Fontes Poppins e Lora)
│   ├── navigation/         # Destinos, rotas base (Enum TopLevelDestination)
│   └── database/           # Configuração base do Room (AppDatabase, DAOs globais)
│
├── feature/                # Módulos de negócio independentes
│   ├── home/               # Dashboard / Tela inicial
│   ├── spots/              # Lista de pontos turísticos e formulários de cadastro
│   └── map/                # Integração com mapas (futuro)
│
└── PicTravellyApp.kt       # Application class (Onde inicializamos a DI Manual)
```

## 🎨 Design System e Padrões de UI

Para garantir consistência e evitar retrabalho, **nunca utilize componentes brutos do Material
Design diretamente nas telas**. Toda a interface deve ser montada usando os *wrappers* disponíveis
em `core/designsystem/components`.

* **Superfícies:** O aplicativo utiliza navegação Edge-to-Edge nativa. Não aplique fundos rígidos em
  `Scaffolds`. Respeite o `paddingValues` injetado pelo Scaffold e utilize espaçamento interno de
  rolagem para que as listas passem transparentes por trás da barra flutuante.
* **Barra de Navegação:** Gerenciada exclusivamente via `currentBackStackEntryAsState()`. Não use
  variáveis de estado soltas para forçar a renderização da aba atual.
* **Tipografia:** A tipografia é híbrida. Títulos utilizam fontes Serifadas (`headlineLarge`) para
  passar um tom de "diário clássico", enquanto descrições e interfaces utilizam fontes Geométricas (
  `bodyLarge`). Isso é resolvido automaticamente ao usar `PicTravellyTitle` e `PicTravellyText`.

## 🚀 Como Executar o Projeto Localmente

1. Clone o repositório.
2. Abra o projeto na versão mais recente do Android Studio.
3. Aguarde o **Gradle Sync**. O download do motor KSP amarrado ao Kotlin 2.4.x pode demorar alguns
   minutos na primeira execução.
4. Certifique-se de ter um emulador ou dispositivo físico rodando Android 10 (API 29) ou superior.
5. Execute o módulo `app`.

## 📋 Próximas Etapas (Roadmap Imediato)

1. **Modelagem de Dados:** Criação da `TouristSpotEntity` e construção dos DAOs no Room.
2. **Container de Injeção:** Implementação do Singleton do Room Database na classe `PicTravellyApp`
   e exposição via Factory para as *features*.
3. **Telas Principais:** Desenvolvimento da lista de Locais (`feature/spots`) consumindo os
   componentes base do Showroom.