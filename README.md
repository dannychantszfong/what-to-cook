# What to Cook

An Android app that uses AI to generate personalized recipes based on the ingredients you have on hand.

## Features

- **Ingredient Management** — Add regular ingredients and seasonings with optional quantities and units. Organize your pantry before generating recipes.
- **AI Recipe Generation** — Send your ingredient list to any supported AI provider and receive 3 diverse recipe suggestions with different cooking methods and cuisines.
- **Favorite Recipes** — Mark generated recipes as favorites and browse them from a dedicated screen.
- **16+ AI Providers** — Works with OpenAI, Anthropic, Google Gemini, Mistral, Cohere, DeepSeek, Grok, AWS Bedrock, Azure OpenAI, OpenRouter, and more. Custom endpoints supported.
- **Local-First Storage** — All data (ingredients, recipes, API keys) is stored on-device via Room. Nothing leaves the device except the ingredient list sent to your chosen AI service.

## Screenshots

<!-- Add screenshots here -->

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material Design 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Database | Room (SQLite) |
| Networking | Retrofit + OkHttp |
| Async | Kotlin Coroutines |
| Navigation | Navigation Compose |

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- Android SDK 35
- A device or emulator running Android 7.0+ (API 24+)
- An API key from any [supported AI provider](#supported-ai-providers)

### Build & Run

1. Clone the repo:
   ```bash
   git clone https://github.com/dannychantszfong/what-to-cook.git
   cd what-to-cook
   ```

2. Open the project in Android Studio.

3. Build and run on your device or emulator.

4. Go to the **Profile** tab, enter your API key and select your preferred AI model, then tap **Save**.

5. Head to the **Ingredients** tab, add what you have, and tap **Generate Recipes**.

## Supported AI Providers

| Provider | Models |
|---|---|
| OpenAI | GPT-4, GPT-4 Turbo, GPT-3.5 Turbo |
| Anthropic | Claude 3 Opus, Sonnet, Haiku |
| Google | Gemini Pro, Gemini Pro Vision |
| Mistral AI | Large, Medium, Small |
| Cohere | Command, Command R, Command R+ |
| DeepSeek | DeepSeek Chat |
| Grok (xAI) | Grok Beta |
| AWS Bedrock | Claude, Titan |
| Azure OpenAI | Custom endpoint |
| OpenRouter | Auto routing |
| Hyperbolic, Novita, Together AI, Fireworks, Replicate | Various |
| Custom | Any OpenAI-compatible endpoint |

## Project Structure

```
app/src/main/java/com/example/what_to_cook/
├── data/               # Domain models (Ingredient, Recipe, AIModel, …)
├── database/           # Room database, DAOs, and entity classes
├── repository/         # Data access layer (ingredient, recipe, API key repos)
├── service/            # AIService — provider abstraction + 16+ handlers
├── ui/
│   ├── screens/        # Composable screens (Ingredients, Recipe, Profile, Favorites)
│   └── theme/          # Color, typography, and theme tokens
└── viewmodel/          # ViewModels for each screen
```

## Privacy

API keys are stored locally in the Room database and never transmitted. Ingredient data is sent only to the AI provider you configure. See [privacy_policy.md](privacy_policy.md) for details.

## License

This project is for personal use. No license is currently specified.
