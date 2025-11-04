# Swipe To Play

**Seu Curador Personalizado de Jogos**

Um aplicativo Android moderno que oferece recomendações personalizadas de jogos da Steam através de uma experiência interativa de swipe, similar ao Tinder, mas para descobrir novos jogos.

## 🎯 Objetivo do Projeto

O **Swipe To Play** foi desenvolvido para resolver o problema comum de descobrir jogos que realmente interessam aos usuários. Com a vasta biblioteca da Steam, encontrar jogos alinhados com suas preferências pode ser uma tarefa desafiadora.

### Principais Objetivos:

- **Descoberta Personalizada**: Oferecer recomendações de jogos baseadas nas preferências, gêneros e categorias favoritas do usuário
- **Experiência Interativa**: Interface intuitiva com gestos de swipe para curtir ou descurtir jogos rapidamente
- **Gamificação**: Sistema que aprende com as interações do usuário para melhorar recomendações futuras
- **Análise de Qualidade**: Apresentar informações sobre toxicidade, bugs e microtransações dos jogos
- **Integração Steam**: Conectar-se com a API da Steam para dados oficiais e atualizados dos jogos

## ✨ Funcionalidades Principais

### 🔐 Autenticação
- Login social com Google OAuth
- Autenticação JWT segura
- Gerenciamento de sessão e tokens

### 🎮 Recomendações de Jogos
- Sistema de recomendação baseado em preferências do usuário
- Interface de swipe para interação rápida
- Detalhes completos dos jogos (descrição, screenshots, requisitos)
- Filtros por gênero, categoria e preferências

### 👤 Perfil e Preferências
- Gerenciamento de perfil do usuário
- Configuração de preferências de jogo
- Seleção de gêneros e categorias favoritas
- Configurações de monetização

### 📱 Interface Moderna
- Design moderno com Jetpack Compose
- Animações suaves e transições
- Tema personalizado e adaptável
- Onboarding para novos usuários

### 💾 Armazenamento Local
- Cache de jogos para melhor performance
- Limite de jogos diários
- Gerenciamento de estado de onboarding
- Preferências de notificações

## 🛠️ Tecnologias Utilizadas

- **Kotlin** - Linguagem principal
- **Jetpack Compose** - UI moderna e declarativa
- **Android Architecture Components**:
  - ViewModel para gerenciamento de estado
  - Repository pattern para camada de dados
  - Coroutines para operações assíncronas
- **Retrofit** - Cliente HTTP para comunicação com API REST
- **Google Sign-In** - Autenticação OAuth
- **Material Design 3** - Componentes de UI
- **SharedPreferences** - Armazenamento local

## 📋 Arquitetura

O projeto segue a arquitetura **Clean Architecture** com separação clara de responsabilidades:

```
app/
├── data/
│   ├── auth/              # Gerenciamento de autenticação
│   ├── local/             # Gerenciadores locais (cache, limites, onboarding)
│   ├── preferences/        # Preferências do usuário
│   ├── remote/            # API services e DTOs
│   └── repository/        # Camada de repositório
├── domain/
│   ├── mapper/            # Mapeadores de dados
│   └── model/             # Modelos de domínio
└── ui/
    ├── components/        # Componentes reutilizáveis
    ├── features/         # Features da aplicação
    │   ├── home/         # Tela principal com swipe
    │   ├── login/        # Autenticação
    │   ├── game/        # Detalhes de jogos
    │   ├── preferences/  # Preferências do usuário
    │   ├── profile/     # Perfil do usuário
    │   └── onboarding/  # Onboarding
    └── theme/           # Temas e estilos
```

## 🚀 Como Usar

### Pré-requisitos

- Android Studio Hedgehog ou superior
- Android SDK 24+ (Android 7.0+)
- Kotlin 1.9.0+
- Gradle 8.0+

### Configuração

1. Clone o repositório:
```bash
git clone <url-do-repositorio>
cd swipe-to-play
```

2. Configure as variáveis de ambiente:
   - Configure o Google OAuth Client ID no `AndroidManifest.xml`
   - Configure a URL da API backend nas configurações do Retrofit

3. Sincronize o projeto no Android Studio

4. Execute o aplicativo

## 📱 Fluxo do Usuário

1. **Onboarding**: Primeiro acesso apresenta o guia do aplicativo
2. **Login**: Autenticação com Google
3. **Preferências**: Configuração inicial de gêneros e categorias favoritas
4. **Home**: Deslize (swipe) pelos jogos recomendados
   - Swipe para direita = Curtir
   - Swipe para esquerda = Descurtir
   - Toque = Ver detalhes
5. **Perfil**: Acesse suas preferências e configurações

## 🔗 Integração com Backend

O aplicativo consome uma API REST desenvolvida em Laravel que fornece:
- Endpoints de autenticação
- Recomendações de jogos personalizadas
- Interações do usuário (likes/dislikes)
- Preferências e configurações
- Dados dos jogos via integração com Steam API

---

**Desenvolvido com ❤️ para gamers que querem descobrir seu próximo jogo favorito**

