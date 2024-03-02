# Ayaya-Bot
###### The source code of my discord bot Ayaya.

Here lies the result of the discord bot project I started back in 2017.
Originally this bot was made from another bot written in Python using the [discord.py](https://github.com/Rapptz/discord.py) wrapper.
For version 2 I decided to rewrite her from scratch in Java using [JDA](https://github.com/discord-jda/JDA).

This is a bot that aims at being cute while providing different functionalities.
The public instance was shut down in the 31st of May 2023 due to lack of funding for the VPS I hosted her on.

If you are interested in running your own private instance of this bot, here's what you need:
- Java 11 or above.
- PostgreSQL.
- A Java IDE that supports Gradle projects. You can check out Intellij, but if you prefer VSCode or Eclipse, install the necessary extensions.
- The necessary configuration. A template can be found at this [repo](https://github.com/Ayaya-Team/Ayaya-Misc), along with information on how to fill the template.

Current issues with this project:
- The project is still using JDA 4, which currently no longer receives regular updates. I am not sure yet if I will convert her to use JDA 5, but in the meantime, JDA 4 still works.
- The project is mostly documented, but some of the code may lack documentation.

###### Short FAQ:

Q: Can I use Java 8 instead?  
A: When I first made Ayaya version 2 in Java, I was using Java 8, but I am not sure if the dependencies currently in use still support it.

Q: Do I need to use the Gradle version in the current configuration?  
A: You can use another version if you want, do note however that the build.gradle file uses the jcenter repository which is currently deprecated and may be unusable in a future major version of Gradle.

Q: Can I use another database management system other than PostgreSQL?  
A: Yes, you have to change the JDBC dependency in build.gradle to the appropriate one for the system you want to use. Do consider that the database dump in [here](https://github.com/Ayaya-Team/Ayaya-Misc) is a dump from a PostgreSQL database, you may need to take extra steps to fill a database in another system with it.

Q: Why did you not use an image API for the gifs?  
A: I am very strict and selective with the gifs I use, so I preferred to have a database with links of the selected gifs.
