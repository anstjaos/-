import hashlib
import json
from time import time
from urllib.parse import urlparse
import os.path
from os import path
import re
import sys
import requests
####### block generation & its principle
class Blockchain(object):
    # initialize the blockchain info
    datas = []
    chain = []
    nodes = set()
    def __init__(self):
        # genesis block
        if self.load() == False:
            self.chain.append({     # genesis block
                'index': 1,
                'timestamp' : time(),  # timestamp from 1970
                'datas': "",
                'merkle_hash': "",
                'previous_hash': "",
                'hash': self.hash(1, time(), "", "", "")
            })

    def cal_merkle_hash(self, count):
        arr = []
        for i in range(0, count):
            arr.append(hashlib.sha256(str(self.datas[i]).encode()).hexdigest())
            print(arr[i])

        while count != 1:
            for i in range(0, count//2):
                arr[i] = hashlib.sha256(str(arr[i*2]).encode() + str(arr[i*2+1]).encode()).hexdigest()

            if count % 2 == 0:
                count = count // 2
            else:
                arr[count//2] = arr[count-1]
                count = count // 2 + 1

        return arr[0]

    def new_block(self, count):
        data = []

        for i in range(0, count):
            print(self.datas[i])
            data.append(self.datas[i])

        merkle_hash = self.cal_merkle_hash(count)

        block = {
            'index': len(self.chain) + 1,
            'timestamp': time(),  # timestamp from 1970
            'datas': data,
            'merkle_hash': merkle_hash,
            'previous_hash': self.chain[-1].get('hash'),
            'hash': self.hash(len(self.chain) + 1, time(), data, merkle_hash,  self.chain[-1].get('hash'))
        }

        return block

    def convey_block(self, block, count):
        verify = 0
        nodes = list(self.nodes)
        inform = {
            'block': block,
            'count': count,
        }

        header = {'content-type': 'application/json'}
        nodes_count = 0

        for i in range(0, len(nodes)):
            try:
                result = requests.post("http://" + nodes[i] + ":5000/blocks/verify", headers=header, data=json.dumps(inform)).json()
                nodes_count += 1
                if result["result"] == "yes":
                    verify += 1
            except Exception as e:
                    print(e)
            except requests.exceptions.RequestException as e:
                print(e)
            except requests.exceptions.NewConnectionError as e:
                print(e)
            except requests.exceptions.ConnectionError as e:
                print(e)

        if verify >= (nodes_count * 2 / 3):
            self.append_block(block, count)
            for i in range(0, len(nodes)):
                try:
                    requests.post("http://" + nodes[i] + ":5000/blocks/new", headers=header, data=json.dumps(inform))
                except Exception as e:
                    print(e)
                except requests.exceptions.RequestException as e:
                    print(e)
                except requests.exceptions.NewConnectionError as e:
                    print(e)
                except requests.exceptions.ConnectionError as e:
                    print(e)
            return "yes"

        return "no"

    def verify_block(self, block, count):
        create_block = self.new_block(count)
        if create_block["datas"] == block["datas"] and create_block["previous_hash"] == block["previous_hash"]:
            return "yes"
        return "no"

    def append_block(self, block, count):
        self.chain.append(block)
        for i in range(0, count):
            print(self.datas[0])
            del self.datas[0]

    def new_transaction(self, key, value):
        print("%s %s", key, value)
        if key[0] == 'v':
            self.datas.append(
                {
                    'key': key,
                    'checked': value
                }
            )
        else:
            self.datas.append(
                {
                    'key': key,
                    'items': value
                }
            )
        return self.last_block['index'] + 1

    def save(self):

        file = open('datas1.txt', 'w', encoding='utf-8')
        for i in self.datas:
            file.write(str(json.dumps(i))+'\n')
        file.close()

        file = open('chains1.txt', 'w', encoding = 'utf-8')
        for i in self.chain:
            file.write(str(json.dumps(i))+'\n')
        file.close()

    def load(self):
        if path.exists("datas1.txt") == True:
            with open('datas1.txt') as f:
                lines = f.read().split("\n")
                for line in lines:
                    if not line:
                        break
                    line = json.loads(line)
                    if line['key'][0] == 'v':
                        data = {
                            'key': line['key'],
                            'checked': line['checked']
                        }
                    else:
                        item_list = []
                        for i in range(0, len(line['items'])):
                            item_list.append({
                                'index': line['items'][i]['index'],
                                'checked': line['items'][i]['checked']
                            })
                        data = {
                            'key': line['key'],
                            'items': item_list
                        }
                    self.datas.append(data)

        if path.exists("chains1.txt") == False:
            return False

        with open('chains1.txt') as f:
            lines = f.read().split("\n")
            for line in lines:
                if not line:
                    break
                line = json.loads(line)
                data_list = []
                for i in range(0, len(line['datas'])):
                    temp = line['datas'][i]
                    if temp['key'][0] == 'v':
                        data = {
                            'key': temp['key'],
                            'checked': temp['checked']
                        }
                    else:
                        item_list = []
                        for j in range(0, len(temp['items'])):
                            item_list.append({
                                'index': temp['items'][j]['index'],
                                'checked': temp['items'][j]['checked']
                            })

                        data = {
                            'key': temp['key'],
                            'items': item_list
                        }

                    data_list.append(data)

                data = {
                    'index': line['index'],
                    'timestamp': line['timestamp'],
                    'datas': data_list,
                    'merkle_hash': line['merkle_hash'],
                    'previous_hash': line['previous_hash'],
                    'hash': line['hash']
                }

                self.chain.append(data)

        return True

    def print_transaction(self, key):
        for i in self.datas:
            print(i)

    def register_node(self, address):
        print("address: %s" % address)
        parsed_url = urlparse(address)
        print(parsed_url)
        self.nodes.add(parsed_url.netloc)  # netloc attribute! network lockation

    def get_result(self, key, item_count, start_time, end_time):
        if key[0] == 'v':
            result = [0] * (item_count+1)
        else:
            result = [[0] * 6 for i in range(item_count+1)]

        last_block = self.chain[-1]

        if last_block["timestamp"] < end_time and len(self.datas) != 0:
            size = len(self.datas)
            block = self.new_block(size)
            self.convey_block(block, size)

        for i in range(0, len(self.chain)):
            cur_block = self.chain[i]
            if cur_block["timestamp"] < start_time:
                continue

            data = cur_block["datas"]
            for j in range(0, len(data)):
                if data[j]["key"] == key:
                    if key[0] == 'v':
                        result[data[j]["checked"]] += 1
                    else:
                        items = data[j]["items"]
                        for k in range(0, len(items)):
                            result[items[k]["index"]][items[k]["checked"]] += 1

        results = {
            'items': result
        }
        print(results)
        return results

    def valid_chain(self, chain):
        last_block = chain[0]
        current_index = 1

        while current_index < len(chain):
            block = chain[current_index]
            print('%s', last_block)
            print('%s', block)
            print("\n---------\n")
            # check that the hash of the block is correct
            if block['previous_hash'] != self.hash(last_block):
                return False
            last_block = block
            current_index += 1
        return True

    def resolve_conflicts(self):
        neighbours = self.nodes
        new_chain = None

        max_length = len(self.chain)  # Our chain length
        for node in neighbours:
            tmp_url = 'http://' + str(node) + '/chain'
            response = requests.get(tmp_url)
            if response.status_code == 200:
                length = response.json()['length']
                chain = response.json()['chain']

                if length > max_length and self.valid_chain(chain):
                    max_length = length
                    new_chain = chain

            if new_chain:
                self.chain = new_chain
                return True

        return False

    # directly access from class, share! not individual instance use it
    @staticmethod
    def hash(index, timestamp, datas, merkle_hash, previous_hash):
        block_string = str(index).encode() + str(timestamp).encode() + str(datas).encode() +\
                       str(merkle_hash).encode() + str(previous_hash).encode()

        return hashlib.sha256(block_string).hexdigest()

    @property
    def last_block(self):
        return self.chain[-1]

    #
    # def pow(self, last_proof):
    #     proof = 0
    #     while self.valid_proof(last_proof, proof) is False:
    #         proof += 1
    #
    #     return proof
    #
    # @staticmethod
    # def valid_proof(last_proof, proof):
    #     guess = str(last_proof + proof).encode()
    #     guess_hash = hashlib.sha256(guess).hexdigest()
    #     return guess_hash[:4] == "0000"  # nonce
