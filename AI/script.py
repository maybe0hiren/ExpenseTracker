from google import genai
from PIL import Image
import json

import os
from dotenv import load_dotenv

load_dotenv()

api_key = os.getenv("API_KEY")
client = genai.Client(api_key=api_key)

def extract(image_path):
    uploaded_file = client.files.upload(file=image_path)
    prompt = (
        "Extract the name of the receiver and amount paid from the following image. Just give me 2 things, name and amount without the rupee sign, separated by comma nothing else."
    )
    response = client.models.generate_content(
        model="gemini-2.5-flash",
        contents=[uploaded_file, prompt]
    )
    full_text = response.text
    parts = full_text.split(",", 1)
    array = [part.strip() for part in parts]
    print(array) 
    jsonData = {
        "receiver": array[0],
        "amount": array[1]
    }
    with open("payment.json", "w") as json_file:
        json.dump(jsonData, json_file, indent=4)

extract("image.jpeg")
