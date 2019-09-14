import hashlib
import json
from time import time
from urlparse import urlparse

import requests
####### block generation & its principle
class Blockchain(object):
    # initialize the blockchain info
    datas = []
    chain = []
    nodes = set()
    def __init__(self):
        # genesis block
        self.chain.append({     # genesis block
            'index': 1,
            'timestamp' : time(),  # timestamp from 1970
            'datas': "",
            'merkle_hash': "",
            'previous_hash': "",
            'hash': self.hash(1, time(), "", "", "")
        })

    def init(self):
        result = requests.post("http://14.45.49.207:5000/nodes/register").json()

        node_list = result["total_nodes"]
        for i in node_list:
            self.nodes.add(i)

        self.chain = result["total_chains"]
        self.datas = result["total_datas"]
        self.nodes.add('14.45.49.207')

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
            except requests.exceptions.RequestException as e:
                self.nodes.remove(nodes[i])
                print(e)
            except requests.exceptions.ConnectionError as e:
                self.nodes.remove(nodes[i])
                print(e)

        if verify >= (nodes_count * 2 / 3):
            self.append_block(block, count)
            for i in range(0, len(nodes)):
                try:
                    requests.post("http://" + nodes[i] + ":5000/blocks/new", headers=header, data=json.dumps(inform))
                except requests.exceptions.RequestException as e:  # This is the correct syntax
                    self.nodes.remove(nodes[i])
                    print(e)
                except requests.exceptions.ConnectionError as e:
                    self.nodes.remove(nodes[i])
                    print(e)
            return "yes"

        return "no"

    def verify_block(self, block, count):
        create_block = self.new_block(count)
        print(create_block)
        print(block)
        if create_block["datas"] == block["datas"] and create_block["previous_hash"] == block["previous_hash"]:
            return "yes"

        return "no"

    def append_block(self, block, count):
        self.chain.append(block)
        for i in range(0, count):
            del self.datas[0]

    def new_transaction(self, key, value):
        print(key, value)
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

    def print_transaction(self, key):
        for i in self.datas:
            print(i)

    def register_node(self, address):
        print("address: %s" % address)
        parsed_url = urlparse(address)
        print(parsed_url)
        self.nodes.add(parsed_url.netloc)  # netloc attribute! network lockation

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

