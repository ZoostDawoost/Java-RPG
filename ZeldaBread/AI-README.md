# 🦙💡 Setting Up Ollama with Mistral on Windows

This guide will walk you through installing and running the **Mistral 7B** AI model locally using [Ollama](https://ollama.com/) on Windows.

---

## 🖥️ System Requirements

- **Operating System**: Windows 10 or Windows 11 (64-bit).
- **RAM**: Minimum 8 GB (16 GB recommended for smoother performance).
- **Storage**: ~4-8 GB free space (models are large).
- **GPU**: Optional — Ollama will use it if supported (for faster inference).

---

## 🔥 Step 1: Download & Install Ollama

1. Visit the official download page:  
👉 [https://ollama.com/download](https://ollama.com/download)

2. Click `Download for Windows` and save the installer.

3. Run the `.exe` installer and follow the setup instructions.

4. Once installed, restart your Command Prompt (`cmd.exe`) or PowerShell.

---

## 💡 Step 2: Pull the Mistral Model

Open **Command Prompt** or **PowerShell** and enter:

```powershell
ollama pull mistral
