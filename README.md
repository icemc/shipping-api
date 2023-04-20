# Hiis ZIO API seed project

![Scala Version](https://img.shields.io/badge/Scala-2.13.8-red)
![SBT Version](https://img.shields.io/badge/SBT-1.7.2-blueviolet)
![Scala CI](https://github.com/hiis-io/zio-starter/actions/workflows/scala.yml/badge.svg)

## Description
This is a seed template for scala microservice applications. This should serve as a base template for most if not all scala microservice applications at hiis. It made up of the following main components

- ZIO (zio, zio-http, etc)
- Authentication and authorization
- Docker build
- Sbt Mdoc for documentation
- Scala formatting (scalafix, scalafmt)

## Structure
This structure is standard across most microservice code bases at Hiis and should be maintained if possible.

- [docs](./docs) contains the project documentation and should be regularly updated.
- [zio-microservice-docs](./zio-microservice-docs) contains the project generated docs (such as scala docs) and [website](./website) contains entire documentation static site. See [mdoc](https://scalameta.org/mdoc/docs/installation.html) and [mdoc with docusaurus](https://scalameta.org/mdoc/docs/docusaurus.html) to learn more.


- [modules/core](./modules/core) should contain all core project components such as models and common code.
- [modules/application](./modules/application) should contain the main zio application.
- [modules/it](./modules/it) should contain the integration test code for the main application.


- [Dockerfile](./Dockerfile) contains build information on how to build the docker image based on the application.

## How to run
- Make sure you have sbt and java 17 installed the type `sbt run` on commandline and open http://localhost:8080/docs/ on your browser

## Building application (SBT assembly & Docker)

- To build the fat jar using sbt assembly simply run the command `sbt build` with your desired configurations set up
  in [`application.conf`](./modules/application/src/main/resources/application.conf)
- To build the docker image simply run `sbt build-docker` with your desired configurations set up
  in [`application.conf`](./modules/application/src/main/resources/application.conf). Make sure you change the docker
  user in [`build.sbt`](./build.sbt)

## How to build jar file
- run `sbt build` the jar file will be created and located in [modules/application/target/scala-2.13/zio-microservice-seed.jar](modules/application/target/scala-2.13/zio-microservice-seed.jar)
- This jar file can be executed on any java platform running java 17

## How to run the jar file
- run `java -jar /modules/application/target/scala-2.13/zio-microservice-seed.jar` and open the browser at [http://localhost:8080/docs](http://localhost:8080/docs)

## Deploy to [Render](https://dashboard.render.com/register)
- Create a Git repository on Github for the project
- Create an account on [Render](https://dashboard.render.com/register). Link your Render account to your Github account.
- Build the jar file using the command specified above. Then copy the jar file from [./modules/application/target/scala-2.13/zio-microservice-seed.jar](./modules/application/target/scala-2.13/zio-microservice-seed.jar) to [./dist/zio-microservice-seed.jar](./build/zio-microservice-seed.jar)
- Push it to your git repository
- Create a new web service on Render and select the repository
- Add an environment variable called PORT with value 8080.

## Deploy on [Fly(Preferred)](https://fly.io/app/sign-in)
- Signup on Fly and download [Flyctl(Command line tool)](https://fly.io/docs/hands-on/install-flyctl/)
- In the projects root directory run `flyctl auth login` to login
- Run `fly launch` to generate the fly.toml file. See [https://fly.io/docs/languages-and-frameworks/dockerfile/](https://fly.io/docs/languages-and-frameworks/dockerfile/)
- Run `fly deploy` to deploy the project. Wait for the deployment to complete this will take a while

## Development
- Clone the project
- Make sure you have `sbt` installed in your computer.
- Import the project in your preferred sbt supported IDE (we recommend Intellij Idea, the community version will suffice)
- Make sure all external dependencies are up and running (See External dependencies section above)
- Start coding and testing. 
- Pull request are welcomed.

## Contributing
This project is open to contributions be it through issues or pull request. Have a look at our [`contribution guide`](./CONTRIBUTING.md) before you get started.


## License
MIT