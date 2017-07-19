# cs0320 Term Project

**Team Members:** Tristin Falk-LeFay, Caleb Kim, Adrian Turcu, Justine Breuch

**Project Idea:**   Analyze coffeeshop sales with external factors in order to find trends and offer future projections.

**Mentor TA:** Jonathan Powell

## Project Requirements

https://docs.google.com/a/brown.edu/document/d/1ZEc0y3UQrXYMfOav86gdTVul56mdAZM9U705udBGd5I/edit?usp=sharing


## Project Specs and Mockup

https://docs.google.com/a/brown.edu/document/d/1TOi81DLT7AqzrPoYLcq7z4Z554gQpwPVwznkYn4k5-I/edit?usp=sharing


## Project Design Presentation

Design Check Presentation:
https://docs.google.com/a/brown.edu/presentation/d/10iBEC0zLMUe6NaIEWxFLXBfD4ajCCzwxckHckqD4MJY/edit?usp=sharing

Demo Day Presentation:
https://docs.google.com/a/brown.edu/presentation/d/1cQQsHQ9aUgZJwbAcT3cwaQFenNfyuqgez85OMUKOxL8/edit?usp=sharing


## How to Build and Run

	To run Baristat, you first have to compile and build the project after cloning it from a GitHub repository. This can be done by running “mvn package” in the terminal from the directory. This will build the project and will also run several of the unit tests that we wrote to check our code (many of the tests were ignored, however, in order to limit the number of calls we were making to external APIs). Once this is done, running “./run --gui” in the terminal will start the connection to the server. Now, open a web browser (preferably Google Chrome) and go to “http://localhost:4567/login,” which will take you to the login page for Baristat. At this point, if you connect with Square, you will be taken to a page with a series of graphs, maps, and figures that were created using sales information from your Square account. For this project, we have created a few Square accounts (one of which is linked to Boston Hubway data, which we use to demonstrate the precision of our statistical analysis). For the sake of keeping account information private, we will not give the login information, so if you would like the username and password for one of these accounts, please let us know and we can provide it.
