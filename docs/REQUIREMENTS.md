# 📋 Requisitos do PicTravelly

## Requisitos Funcionais (RF)

| ID       | Nome                               | Descrição                                                                                                                                                                                                   |
|:---------|:-----------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **RF01** | **Cadastrar Local Rápido**         | O usuário deve poder registrar o local atual rapidamente através de um CTA gigante (Call to Action). O app deve tentar capturar as coordenadas GPS automaticamente ao abrir a tela de cadastro.             |
| **RF02** | **Paginação do Histórico**         | A tela de lista deve carregar os locais de forma paginada (ex: 20 por vez) para economizar memória. Cada item deve exibir: miniatura da foto, título, trecho da descrição e a localidade (cidade/endereço). |
| **RF03** | **Visualização Imersiva (Diário)** | Ao clicar em um local, o usuário verá o conteúdo em formato de diário. O mapa mostrará a localização com um PIN (usando mapa offline salvo ou online interativo).                                           |
| **RF04** | **Gestão de Mídia**                | O usuário deve poder visualizar as fotos do diário em tela cheia (Fullscreen) e exportar/salvar as imagens na galeria pública do celular.                                                                   |
| **RF05** | **Gestão do Diário (CRUD)**        | Deve ser possível editar textos, adicionar/remover fotos ou excluir completamente registros históricos do diário.                                                                                           |
| **RF06** | **Busca, Filtro e Ordenação**      | O histórico deve permitir pesquisa textual, filtros (ex: por datas) e ordenações (Mais recentes, Mais antigos, A-Z).                                                                                        |
| **RF07** | **Configurações Internacionais**   | O usuário pode alterar o idioma da interface (Português, Inglês, Espanhol) e o Tema (Claro, Escuro, Sistema).                                                                                               |
| **RF08** | **Mapa Global (Home)**             | A tela inicial deve possuir um mapa interativo geral que plota TODOS os locais já cadastrados pelo usuário.                                                                                                 |
| **RF09** | **Gamificação Visual**             | A Home deve exibir *insights* (estatísticas de viagens) e um sistema visual de gamificação baseado na quantidade de locais cadastrados (ex: Níveis, Conquistas ou Barras de Progresso).                     |
| **RF10** | **Preferências de Mapa**           | O usuário pode salvar o tipo de mapa (Satélite, Relevo, Padrão) e o nível de Zoom nas configurações; o app deve lembrar essas escolhas via cache local.                                                     |

## Requisitos Não-Funcionais (RNF)

| ID        | Nome                             | Descrição                                                                                                                                                                 |
|:----------|:---------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **RNF01** | **Offline-First Absoluto**       | Todos os textos e fotos operam 100% offline via Room Database. Mapas na tela de detalhes devem exibir um *Snapshot* (imagem estática) caso o dispositivo esteja sem rede. |
| **RNF02** | **Persistência de Preferências** | As configurações do usuário (tema, idioma, mapa) devem ser salvas utilizando o **Jetpack DataStore (Preferences)**, separadas do banco relacional.                        |
| **RNF03** | **Desempenho Visual e Memória**  | O carregamento de imagens pesadas deve ser feito de forma assíncrona com `Coil`, e a lista principal deve utilizar `Paging3` para evitar vazamento de memória.            |