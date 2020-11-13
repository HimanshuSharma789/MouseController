from flask import Flask
from flask import request, jsonify, make_response
import pyautogui
from gevent.pywsgi import WSGIServer
from gevent import monkey

monkey.patch_all()

app = Flask(__name__)
app.debug = True

@app.route('/click', methods=["POST"])
def click():
    if request.is_json:
        req = request.get_json()
        #print(req)


        value = (req.get('value'))
        if(value==1):
            pyautogui.click(button='left')
        elif(value==2):
            pyautogui.click(button='right')
        elif(value==3):
            x, y = pyautogui.position()
            #print(x,y, sep=" ")
            pyautogui.move(int(req.get('xVelocity')), int(req.get('yVelocity')))
        
        return jsonify({"message": "JSON"})
        #return make_response(jsonify({"message": "JSON"}), 200)
        #return "valid-200"
    else:
        return jsonify({"message": "NOT JSON"})
        #return make_response(jsonify({"message": "not JSON"}), 400)
        #return "not-valid-400"

@app.route('/')
def check():
    return "hello world"


def main():
    "Start gevent WSGI server"
    # use gevent WSGI server instead of the Flask
    http = WSGIServer(('0.0.0.0', 5000), app.wsgi_app)
    # TODO gracefully handle shutdown
    http.serve_forever()


if __name__ == '__main__':
    main()
