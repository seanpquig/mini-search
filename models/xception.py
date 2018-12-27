import os
import numpy as np
from keras.applications.xception import Xception, preprocess_input, decode_predictions
from keras.preprocessing import image


def get_predictions(img_path):
    img = image.load_img(img_path, target_size=(299, 299))
    images_array = np.expand_dims(image.img_to_array(img), axis=0)
    images_array = preprocess_input(images_array)
    return model.predict(images_array)

script_path = os.path.dirname(os.path.realpath(__file__))

model = Xception(include_top=True, weights='imagenet')
model.save('{}/xception.h5'.format(script_path))

print(decode_predictions(get_predictions("/Users/seanq/Downloads/cat.jpg")))