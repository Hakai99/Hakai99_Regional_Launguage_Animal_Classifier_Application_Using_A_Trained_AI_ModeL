#  Assamese Animal Detector - Android App

An intelligent, real-time animal detection Android application built with TensorFlow Lite and YOLOv8, featuring Assamese language support and text-to-speech functionality for educational purposes.

##Features

- Real-Time Animal Detection**: Uses a custom-trained YOLOv8 nano model to detect 11 different Assamese animals in real-time through your device's camera
- Assamese Language Support**: Complete Assamese interface with all text, labels, and speech output in Assamese script
- Text-to-Speech Integration**: Automatic pronunciation of detected animals in Assamese using Android's built-in TTS engine
- Offline Functionality**: Works completely offline - no internet connection required for detection
- Lightweight & Fast**: Optimized TensorFlow Lite model ensures smooth performance even on budget devices
- Kid-Friendly Interface**: Colorful, intuitive UI designed specifically for children to explore and learn about animals

## Supported Animals

The app detects 11 Assamese animals:
1. হাঁহ (Duck)
2. মেকুৰী (Parrot)
3. গৰু (Cow)
4. কুকুৰ (Dog)
5. হাঁহ (Duck)
6. হাতী (Elephant)
7. লগনিয়া (Monkey/Langur)
8. পাৰ চৰাই (Bird)
9. ম'হ (Bufallow)
10. কাছ (Turtle)

## Technology Stack

- **Language**: Kotlin
- **Framework**: Android SDK (API 21+)
- **ML Model**: YOLOv8 Nano (Custom Trained)
- **ML Framework**: TensorFlow Lite
- **UI**: AndroidX with Material Design
- **Libraries**: CameraX, Coroutines, OkHttp

## Requirements

- Android 5.0 (API 21) or higher
- Camera permission
- Internet permission (for Wikipedia integration)
- Minimum 100MB storage

## How It Works

1. Launch the app and grant camera permissions
2. Point your device at an animal
3. The model detects the animal in real-time
4. App displays the animal name in Assamese
5. Text-to-Speech pronounces the name
6. Users can explore more information via Wikipedia search

## Model Performance

- Training Data: 873 images across 11 animal classes
- Validation Accuracy: 87.6% mAP@50
- Inference Speed: 2.5ms per image
- Model Size: 11.7MB (TFLite)

## 📝 License

This project is open-source and available for educational purposes.

## 👨‍💻 Author
Dev Kumar Lahkar has
developed as an educational tool to teach children about animals in their native Assamese language.

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Feel free to fork and submit pull requests.

---

Learn animals in Assamese. Offline. Real-time. Free.
