import os
import numpy as np
from keras.applications.imagenet_utils import preprocess_input, decode_predictions
from keras.applications.inception_v3 import InceptionV3
from keras.applications.xception import Xception
from keras.applications.nasnet import NASNetLarge, NASNetMobile
from keras.preprocessing import image


def get_predictions(img_path, img_dim):
    img = image.load_img(img_path, target_size=(img_dim, img_dim))
    images_array = np.expand_dims(image.img_to_array(img), axis=0)
    images_array = preprocess_input(images_array, mode='tf')
    return model.predict(images_array)

script_path = os.path.dirname(os.path.realpath(__file__))
test_image = "/Users/seanq/Downloads/cat.jpg"

model_data = [
    ("inception_v3", InceptionV3, 299),
    ("xception", Xception, 299),
    ("nasnet_large", NASNetLarge, 331),
    ("nasnet_mobile", NASNetMobile, 224),
]

for name, model_cls, img_width in model_data:
    print('\nLoading and saving model: {}'.format(name))
    model = model_cls(include_top=True, weights='imagenet')

    # Save full model as h5 file
    model.save('{}/{}.h5'.format(script_path, name))

    # Get and display model predictions for test image
    preds = get_predictions(test_image, img_width)
    print('predictions for model:')
    print(decode_predictions(preds))
