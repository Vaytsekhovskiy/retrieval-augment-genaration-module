from PIL import Image
import torch
import pandas as pd
import numpy as np
from torchvision import transforms
import argparse
import torchvision
import torch
import torch.nn as nn
import torch.nn.functional as F
import sys
import codecs
import torch
import torch.nn as nn
import torch.nn.functional as F

class CNNForClassification(nn.Module):
    def __init__(self):
        super(CNNForClassification, self).__init__()

        self.conv1 = nn.Conv2d(4, 16, kernel_size=3, padding=1)  # Вход: 3 канала (RGB), выход: 16
        self.pool = nn.MaxPool2d(kernel_size=2, stride=2)       # Пулинг с размером 2x2
        self.conv2 = nn.Conv2d(16, 32, kernel_size=3, padding=1) # Вход: 16 каналов, выход: 32
        self.fc1 = nn.Linear(32 * 56 * 56, 128)                 # Приведение к размерности для fc слоя
        self.fc2 = nn.Linear(128, 2)                             # Выход на 2 класса (0 и 1)

    def forward(self, x):
        x = self.pool(F.relu(self.conv1(x)))                    # Применяем ReLU после первого слоя
        x = self.pool(F.relu(self.conv2(x)))                    # Применяем ReLU после второго слоя
        x = x.view(-1, 32 * 56 * 56)                             # Приводим к нужной форме для полносвязного слоя
        x = F.relu(self.fc1(x))
        x = self.fc2(x)
        return x

def convertTo4Channel(pathToImage: str):
    img = torchvision.io.read_image(pathToImage)
    transofrm = torchvision.transforms.Resize(size = (224,224))
    if(img.shape[0] == 3):
        new_channel = torch.zeros(1, 224, 224)
        img_tensor =  transofrm(img)
        img_tensor = torch.cat((img_tensor, new_channel), dim = 0)
    if(img.shape[0] == 4):
        img_tensor =  transofrm(img)
    return img_tensor/255


model = CNNForClassification()
parser = argparse.ArgumentParser()
parser.add_argument("input")
args = parser.parse_args()
pathToImage = args.input
model.load_state_dict(torch.load("ImageClassifier.pth"))
result = model(convertTo4Channel(pathToImage)).argmax()
print(int(result.bool()))
