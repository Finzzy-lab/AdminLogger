# AdminLogger

A simple but powerful admin command logger for Minecraft servers using Spigot/Paper API.  
This plugin monitors OP/admin commands like `/gamemode`, `/give`, `/tp`, and logs them publicly in chat for better transparency and moderation.

## 🔧 Features

- Logs creative item pickups
- Detects and broadcasts gamemode changes
- Custom logs for commands like:
  - `/give`, `/tp`, `/kill`, `/ban`, `/kick`, `/op`, `/deop`, `/summon`, etc.
- Broadcasts logs in colorful chat messages
- Logs also appear in server console
- Easily extendable!

## 🌍 Localization

- ✅ English (Default)
- ✅ Turkish (`lang: tr` in `config.yml`)

More languages can be added via `lang/` folder.

## 📦 Installation

1. Download the plugin `.jar` file.
2. Place it in your server's `/plugins` folder.
3. Start or reload your server.
4. (Optional) Edit `config.yml` to set language.

## 🧪 Commands

This plugin has no player commands. It passively listens to admin actions.

## 🔒 Permissions

Only actions by OP players (`operator`) are logged.

## 📁 File Structure

