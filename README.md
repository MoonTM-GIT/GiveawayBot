<img align="right" src="https://github.com/MoonTM-GIT/GiveawayBot/blob/main/.github/icon.png?raw=true" height="200" width="200">

# GiveawayBot

A Discord Giveaway Bot initially based on the [JavaBot](https://github.com/Java-Discord/JavaBot), now using [Dynxsty's Interaction Handler](https://github.com/DynxstyGIT/DIH4JDA) for Interactions.

Still WIP, will be fully released when all planned features are implemented.

# Giveaway Creation
To start a giveaway, use the `/giveaway create` command in a text channel. This will open a modal similar to this:

<img src="https://github.com/MoonTM-GIT/GiveawayBot/blob/main/.github/modal.png?raw=true" height="436" width="437">

- **Giveaway Prize**\
 Enter what you want to give away here.
- **Amount of Winners**\
 Enter the amount of winners here.
- **Giveaway End Date**\
 Enter the date you want the giveaway to end (**in UTC**) here. The date should have the following format: `dd/MM/YYYY HH:mm` (e.g. 24/12/2020 18:00).

When you've done that, press the `Submit` button and the giveaway will be created in the channel you were in when you executed `/giveaway create`.

# Setup

To start up, run the bot once, stop it, and it will generate a `systems.json` file in the `/config` directory.
For the Bot to start, the following values need to be set:

- `jdaBotToken` to a Discord Bot Token
- `botInviteLink` to any valid Link (preferably the Bot's invite link)
- `giveawayConfig.participateEmoteId` to an emoji-id

Note that this is just what is required for the Bot to start, other features will probably require a few other values to be set.

