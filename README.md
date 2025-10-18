# GitHub Copilot Chat Export Plugin for IntelliJ IDEA

<!-- Plugin description -->
Export GitHub Copilot Chat conversations to MD files for easy reference and sharing.
<!-- Plugin description end -->

## Features
- Easily export your GitHub Copilot Chat conversations
- Save conversations as Markdown files
- Maintain conversation history for future reference
- Simple and intuitive user interface

## Installation

### Using the IDE built-in plugin system:
1. Go to <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd>
2. Search for <kbd>"Github Copilot Chat Exporter"</kbd>
3. Click <kbd>Install</kbd>

### Using JetBrains Marketplace:
- Visit the [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/25208-github-copilot-chat-exporter) and click <kbd>Install to ...</kbd> if your IDE is running
- Alternatively, download the [latest release](https://plugins.jetbrains.com/plugin/25208-github-copilot-chat-exporter/versions) and install manually via
<kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

### Manual Installation:
- Download the [latest release](https://github.com/enciyo/gh-che-idea-plugin/releases/latest)
- Install manually: <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Usage
1. Open GitHub Copilot Chat in your IntelliJ IDEA
2. Have a conversation with Copilot
3. Open the **GithubCopilotChatExporter** tool window (sidebar)
4. Configure your export settings:
   - **Author**: Name that will appear in the exported Markdown file (defaults to Git config user.name)
   - **File Name**: Name for the exported .md file (defaults to current branch name)
   - **Regex for chat title**: Optional regex to extract a custom title from branch name
   - **Use Regex**: Enable regex processing for file naming
5. Click **Export** to save the conversation as a Markdown file
6. Files are saved to `/ai/copilot/prompts/` directory in your project
7. Access your saved conversations anytime

## Requirements
- IntelliJ IDEA or compatible JetBrains IDE
- GitHub Copilot subscription
- GitHub Copilot Chat enabled

## Support
If you encounter any issues or have suggestions, please [create an issue](https://github.com/enciyo/gh-che-idea-plugin/issues) on our GitHub repository.

## License
This project is licensed under the terms specified in the repository.

---