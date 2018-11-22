from keras.applications.inception_v3 import InceptionV3
import os

script_path = os.path.dirname(os.path.realpath(__file__))

model = InceptionV3()
model.save('{}/inceptionV3.h5'.format(script_path))
