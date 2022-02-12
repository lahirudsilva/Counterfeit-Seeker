import os
import keras_preprocessing.image
from flask import Flask, request
from tensorflow.keras.models import load_model
import numpy as np

app = Flask(__name__)

label_names = ['$10 bill : Counterfeit',
               '$10 bill : Genuine ',
               '$100 bill : Counterfeit',
               '$100 bill : Genuine',
               '$20 bill : Counterfeit',
               '$20 bill : Genuine',
               '$50 bill : Counterfeit',
               '$50 bill : Genuine']


dir_path = os.path.dirname(os.path.realpath(__file__))
UPLOAD_FOLDER = "uploads"
STATIC_FOLDER = "static"

# load model
model = load_model('final_banknote_auth_main.h5')
IMAGE_SIZE = 224


# Process image and predict label
def process_img(IMG_PATH):
    # Preprocess image
    image = keras_preprocessing.image.load_img(IMG_PATH, target_size=(IMAGE_SIZE, IMAGE_SIZE))
    image = keras_preprocessing.image.img_to_array(image)
    image = image.reshape(1, IMAGE_SIZE, IMAGE_SIZE, 3)

    res = model.predict(image)
    label = np.argmax(res)
    print("Label", label)
    labelName = label_names[label]
    print("Predicted Label name:", labelName)
    return labelName


@app.route('/')
def hello_world():  # put application's code here
    return 'Hello World!'


@app.route('/predict', methods=['POST', 'GET'])
def prediction():
    if request.method == "POST":
        file = request.files["image"]
        upload_image_path = os.path.join(UPLOAD_FOLDER, file.filename)
        print(upload_image_path)
        file.save(upload_image_path)

        response = process_img(upload_image_path)

        return response


if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000, debug=True)
