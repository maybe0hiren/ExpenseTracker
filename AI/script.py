from flask import Flask, request, jsonify
import requests

from google import genai
from PIL import Image
import json

import os
from dotenv import load_dotenv
load_dotenv()

api_key = os.getenv("API_KEY")
client = genai.Client(api_key=api_key)

app = Flask(__name__)

@app.route('/process-image', methods=['POST'])
def extract():
    if 'image' not in request.files:
        return jsonify({
            "error: Image not found"
        })
    image = request.files['image']
    image.save(os.path.join("tempStorage", "image.jpeg"))
    
    image_path = os.path.join("tempStorage", "image.jpeg")
    uploaded_file = client.files.upload(file=image_path)
    prompt = (
        "Extract the name of the receiver, date of transaction and amount paid from the following image. Just give me 3 things, name, date (exclude the time too) and amount without the rupee sign, separated by comma nothing else. If the image is not valid, return INVALID text for all 3 parameters"
    )
    response = client.models.generate_content(
        model="gemini-2.5-flash",
        contents=[uploaded_file, prompt]
    )
    full_text = response.text
    parts = full_text.split(",", 2)
    array = [part.strip() for part in parts]
    print(array) 
    return jsonify({
        "receiver": array[0],
        "date": array[1],
        "amount": float (array[2])
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)