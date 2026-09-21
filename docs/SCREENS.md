# 📱 Mapeamento de Telas (UI/UX)

A navegação base é gerenciada pela barra flutuante animada. O roteamento utiliza o Navigation
Compose.

## 1. Home / Dashboard (`home_route`)

**A Alma Gamificada do App.**

* **Header Estatístico:** Número gigante destacando o total de locais visitados.
* **Gamificação:** Componentes visuais como barra de experiência (XP), título do usuário (ex: "
  Viajante Explorador") ou "Selos" desbloqueados por marcos de cadastro.
* **Mapa Global:** Um componente de Google Map ocupando parte significativa da tela, exibindo
  múltiplos PINs com todos os locais do histórico.
* **Shortcuts:** Cards ou botões rápidos redirecionando para os últimos locais adicionados ou
  abrindo a tela de listagem.

## 2. Diários e Locais (`spots_route`)

**A Gestão do Acervo Histórico.**

* **CTA Principal:** Um botão flutuante (FAB) super destacado escrito "Cadastrar Local Atual", que
  puxa o GPS e abre a tela de cadastro.
* **Barra de Ferramentas:** Input de pesquisa (busca por título) e ícones para filtragem/ordenação.
* **Lista Paginada:** Lista vertical dinâmica. Cada `PicTravellyCard` exibe:
* Imagem de capa em miniatura.
* Título da viagem.
* Nome do local/cidade com ícone de pino.
* Uma ou duas linhas descritivas truncadas com reticências.

## 3. Tela de Configurações / Menu (`menu_route`)

**O Controle do Aplicativo.**

* **Secão de Idioma:** Segmented Button ou Dropdown para alternar (PT-BR, EN-US, ES-ES).
* **Secão de Aparência:** Botões de rádio para Forçar Claro, Forçar Escuro ou Seguir Sistema.
* **Secão do Mapa:** Sliders para Zoom Padrão e opções de tipo visual do mapa (Satélite, Terreno).

## 4. Cadastro / Edição de Diário (Navegação Interna)

* **Ação:** Disparada pelo FAB da tela de Locais.
* **Elementos:**
* Inputs textuais expansíveis para título e relato.
* Componente de upload de fotos em grade.
* Preview do mapa. Se o GPS estiver ativado, crava o PIN automaticamente. Permite arrastar o mapa
  para ajuste fino.

## 5. Visualização do Diário / Detalhes (Navegação Interna)

**A Imersão na Lembrança.**

* **Fotos em Tela Cheia:** Clique na galeria abre um visualizador Modal interativo (com suporte a
  *pinch-to-zoom* e botão "Salvar no dispositivo" usando Scoped Storage).
* **Leitura Textual:** Relato renderizado com a tipografia Serifada do Design System para imitar
  leitura de diário clássico.
* **Mapa de Contexto:** Bloco contendo o mapa. Lógica em tempo real: Se online, exibe o *GoogleMap*
  interativo com o título do local no PIN; Se offline, exibe a imagem (`offlineMapSnapshotUri`)
  salva como contingência.
* **Menu Flutuante:** Opções no canto superior da tela para "Editar Diário" ou "Apagar Registro".