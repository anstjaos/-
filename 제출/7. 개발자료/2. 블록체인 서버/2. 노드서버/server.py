from flask import Flask, jsonify, request
import flask
import requests
import json
import socket
from textwrap import dedent
from uuid import uuid4

from blockchain import Blockchain

app = Flask(__name__)

node_identifier = str(uuid4()).replace('-', '')
blockchain = Blockchain()


@app.route('/blocks/mine', methods=['POST'])
def mine():
    print("hello")
    values = request.get_json()
    count = values["count"]

    block = blockchain.new_block(count)
    results = blockchain.convey_block(block, count)

    if results == "no":
        blockchain.init()

    response = {
        'result': results,
    }
    return jsonify(response), 200


@app.route('/blocks/verify', methods=['POST'])
def verify_block():
    values = request.get_json()
    block = values["block"]
    count = values["count"]
    results = blockchain.verify_block(block, count)
    print(results)
    response = {
        'result': results,
    }
    return jsonify(response), 200


@app.route('/blocks/new', methods=['POST'])
def append_block():
    values = request.get_json()
    block = values["block"]
    count = values["count"]
    blockchain.append_block(block, count)
    response = {
        'msg': "new block"
    }
    return jsonify(response), 200


@app.route('/transactions/new', methods=['POST'])
def new_transaction():
    values = request.get_json()

    if values["key"][0] == 'v':
        blockchain.new_transaction(values["key"], values["checked"])
    else:
        blockchain.new_transaction(values["key"], values["items"])

    response = {'message': 'Transaction will be added to Block'}

    print(blockchain.datas)
    return jsonify(response), 201


@app.route('/transactions/check', methods=['GET'])
def print_transactions():
    response = {
        'datas': blockchain.datas,
        'length': len(blockchain.chain),
    }
    return flask.jsonify(response), 200


@app.route('/chain', methods=['GET'])
def full_chain():
    ip_address = request.remote_addr
    response = {
        'chain': blockchain.chain,
        'length': len(blockchain.chain)
    }
    print(ip_address)
    return flask.jsonify(response), 200


@app.route('/test', methods=['POST'])
def printRequest():
    print(request.json)
    response = {
        'result': "ok"
    }
    return flask.jsonify(response), 200


@app.route('/nodes/register', methods=['POST'])
def register_nodes():
    values = request.get_json()
    ip_address = values["node"]
    #blockchain.nodes.add(ip_address)
    print(ip_address)
    response = {
        'message': 'New nodes have been added',
    }

    blockchain.register_node("https://"+ip_address)
    print(blockchain.nodes)

    return jsonify(response), 201


@app.route('/nodes/resolve', methods=['GET'])
def consensus():
    replaced = blockchain.resolve_conflicts() # True False return

    if replaced:
        response = {
            'message' : 'Our chain was replaced',
            'new_chain' : blockchain.chain
        }
    else:
        response = {
            'message' : 'Our chain is authoritative',
            'chain' : blockchain.chain
        }

    return jsonify(response), 200

if __name__ == '__main__':
    blockchain.init()
    app.run(host='0.0.0.0', port = 5000)