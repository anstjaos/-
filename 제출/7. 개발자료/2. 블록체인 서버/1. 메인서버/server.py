from flask import Flask, jsonify, request
import flask
import requests
import json
from textwrap import dedent
from uuid import uuid4

from blockchain import Blockchain

app = Flask(__name__)

node_identifier = str(uuid4()).replace('-', '')
blockchain = Blockchain()
cur_count = 0

'''
@app.route('/mine', methods=['GET'])
def mine():
    last_block = blockchain.last_block
    last_proof = last_block['proof']

    proof = blockchain.pow(last_proof)

    blockchain.new_transaction(
        key='0',
        value=node_identifier,
    )

    previous_hash = blockchain.hash(last_block)
    block = blockchain.new_block(proof, previous_hash)

    response = {
        'message': 'new block forged',
        'index': block['index'],
        'transactions': block['transactions'],
        'proof': block['proof'],
        'previous_hash': block['previous_hash']
    }
    return jsonify(response), 200
'''


@app.route('/blocks/verify', methods=['POST'])
def verify_block():
    values = request.get_json()
    block = values["block"]
    count = values["count"]
    results = blockchain.verify_block(block, count)

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
        'result': "ok"
    }
    return jsonify(response), 201


@app.route('/transactions/new', methods=['POST'])
def new_transaction():
    values = request.get_json()
    print(values)
    index = 0
    if values["key"][0] == 'v':
        index = blockchain.new_transaction(values["key"], values["checked"])
    else:
        index = blockchain.new_transaction(values["key"], values["items"])

    header = {'content-type': 'application/json'}
    nodes = list(blockchain.nodes)
    for i in range(0, len(blockchain.nodes)):
        try:
            requests.post("http://" + nodes[i] + ":5000/transactions/new", headers=header, data=json.dumps(values))
        except Exception as e:
            print(e)
        except requests.exceptions.RequestException as e:
            print(e)
        except requests.exceptions.NewConnectionError as e:
            print(e)
        except requests.exceptions.ConnectionError as e:
            print(e)

    response = {'message': 'Transaction will be added to Block {0}'.format(index)}
    if len(blockchain.datas) >= 8:
        count = {
            'count': 8
        }
        global cur_count
        while 1:
            if cur_count < len(blockchain.nodes):
                try:
                    print(nodes[cur_count])
                    r = requests.post("http://" + nodes[cur_count] + ":5000/blocks/mine", headers=header, data=json.dumps(count)).json()
                    cur_count += 1
                    print(r["result"])
                    if r["result"] == "yes":
                        break
                except Exception as e:
                    print(e)
                except requests.exceptions.RequestException as e:
                    print(e)
                except requests.exceptions.NewConnectionError as e:
                    print(e)
                except requests.exceptions.ConnectionError as e:
                    print(e)
            else:
                block = blockchain.new_block(8)
                blockchain.convey_block(block, 8)
                cur_count = 0
                break

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
def print_request():
    response = {
        'res': "ok",
    }
    return flask.jsonify(response), 200


@app.route('/nodes/register', methods=['POST'])
def register_nodes():
    ip_address = request.remote_addr
    print(ip_address)

    response = {
        'message': 'New nodes have been added',
        'total_nodes': list(blockchain.nodes),
        'total_chains': blockchain.chain,
        'total_datas': blockchain.datas,
    }

    header = {'content-type': 'application/json'}
    new_node = {
        'node': ip_address,
    }
    node_list = list(blockchain.nodes)
    for i in range(0, len(node_list)):
        try:
            requests.post("http://" + node_list[i] + ":5000/nodes/register", headers=header, data=json.dumps(new_node))
        except Exception as e:
            print(e)
        except requests.exceptions.RequestException as e:
            print(e)
        except requests.exceptions.NewConnectionError as e:
            print(e)
        except requests.exceptions.ConnectionError as e:
            print(e)

    blockchain.register_node("https://"+ip_address)
    print(blockchain.nodes)

    return jsonify(response), 201


@app.route('/files/save', methods=['GET'])
def file_save():
    blockchain.save()
    response = {
        'msg' : "File save complete!"
    }
    return jsonify(response), 200


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


@app.route('/chain/results', methods=['POST'])
def get_result():
    if len(blockchain.chain) == 1:
        return jsonify({"msg" : "null"})
    values = request.get_json()
    key = values["key"]
    item_count = values["item_count"]
    start_time = values["start_time"]
    end_time = values["end_time"]

    results = blockchain.get_result(key, item_count, start_time, end_time)
    print(results)
    return jsonify(results), 201


if __name__ == '__main__':
   # blockchain.load()
    app.run(host='0.0.0.0')
