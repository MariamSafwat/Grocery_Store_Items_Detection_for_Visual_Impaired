# Grocery_Store_Items_Detection_for_Visual_Impaired

## Overview
This project is the graduation project of the AI-Pro post-graduate diploma developed by the Information Technology Institute (ITI) in partnership with the French Graduate School of Computer Science and Advanced Technologies (EPITA: Ecole d'Ingénieurs en Informatique). <br />
Using Deep learning, NLP, and Mobile App Development this android app helps the visual impaired people at the grocery store by classifying grocery store items and play their description to the user if needed.

## Dataset
### Source
The images of this dataset are from https://github.com/marcusklasson/GroceryStoreDataset <br/>
It was re-arranged to fit the goals of this project, you can find the dataset after re-arrangement in Dataset/Dataset_Rearranged

## Image Classification Models
### Base Model
The Xception proved to be the best model with F1-score 98% on the test set

### Fruits Model
The Xception proved to be the best model with F1-score 97% on the test set

### Vegetables Model
The MobileNet proved to be the best model with F1-score 94% on the test set

### Packages Model
The VGG19 proved to be the best model with F1-score 96% on the test set

## Text to Speech Model


## Speech Recognition Model


## Android App Deployment
### Version 1
How it works:<br />
● Use the 4 models saved as TFLite models<br />
● Use 4 labels text files that contains the names of the objects in each class<br />
● Get the index of the highest value from the first model output array<br />
● Get class name corresponding to that index from labels text file<br />
● Repeat last 2 steps for selected model depending on class name (Fruits, Packages, or Vegetables)<br />
● Use Text to Speech built-in class in android to speak out the name of the predicted object<br />
● Ask the user if they want to hear the full description of the object<br />
● Use Speech Recognition built-in class in android to recognize user response<br />
● If the response is recognized as “Yes” speak out the description of the predicted object<br />

### Version 2
How it works:<br />
● Upload captured image to firebase storage<br />
● Make prediction and send the predicted object name to Text to Speech model <br />
● Upload the audio file containing the object name to the firebase storage <br />
● Retrieve it to android and play it to the user<br />
● Ask the user if they want to hear the full description of the object<br />
● Upload the user response as audio file to firebase storage<br />
● Send it to Speech Recognition model<br />
● If the response is recognized as “Yes” upload the object description to firebase storage as audio file<br />
● Retrieve it to android and play it to the user<br />


## Team Members
 ``` 
 ● Asmaa Mohamed
 ● Asmaa Ali 
 ● Mariam Gendy 
 ● Nahla Tarek
 ● Shefaa Saied
 ● Suzan Adel
 ```
 
 ## Demo Link
 
