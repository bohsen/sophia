import argparse
import os
import time
import datetime
import json
import subprocess
import pathlib
import re
import getpass

debug = False


class PatientHelper:
    ONE_PATIENT_RE = r"{\"medicalInformationId\":\d+,\"personalInformationId\":\d+,\"userRef\":\".+\"}"
    ALL_PATIENTS_RE = rf"(\[{ONE_PATIENT_RE}(,{ONE_PATIENT_RE})*\])"

    MAX_P_REF_LENGTH = 30

    @staticmethod
    def read(path, recurse, regex_file):
        """
        :param path: Folder of fastq files
        :param recurse:
        :param regex_file:
        :return: dict of {patient_ref_01: {sample_id_01: [tag, file1, file2], sample_id_02: [tag, file1, file2]}, . . .}
        """

        print("Reading FastQ folder")

        if os.path.exists(path):
            patient_data = PatientHelper._sort_patients(PatientHelper._read(path, recurse, regex_file))
        else:
            print(f"Error: Couldn't find {path}")
            patient_data = None

        return patient_data

    @staticmethod
    def _read(path, recurse, regex_file):
        """
        Get the patient info of fastq files in path. If recurse is True, recurse to subfolders
        :param path:
        :param recurse:
        :param regex_file:
        :return:
        """

        # Check sub folders if recursion is set
        if recurse:
            fastq_files = list(pathlib.Path(path).rglob("*"))
        else:
            fastq_files = list(pathlib.Path(path).glob("*"))

        if len(fastq_files) == 0:
            print("Error: no files found")
            patient_data = None
        else:
            print(f"Found {len(fastq_files)} files")
            patient_data = PatientHelper._get_patient_data(fastq_files, regex_file)

        return patient_data

    @staticmethod
    def _get_patient_data(filenames, regex_override=None):
        """
        :param filenames: list of fastq filenames
        :return: dict of {patient_ref: sample_id1: [tag, file1, file2], sample_id2: [tag, file1, file2], . . . }
        """

        patients = dict()
        regex_helper = RegexHelper(regex_override)
        for file in filenames:
            file = str(file)
            updated = regex_helper.update(file, patients)
            if not updated:
                print(f"Skipping {file}")

        return patients

    @staticmethod
    def update_patient(patients, p_ref, s_id, tag, file):
        """
        Add/update patient info in patients dict
        :param patients:
        :param p_ref:
        :param s_id:
        :param file:
        :param tag:
        :return:
        """
        if p_ref in patients:
            if s_id in patients[p_ref]:
                patients[p_ref][s_id].append(file)
            else:
                patients[p_ref][s_id] = [tag, file]
        else:
            patients[p_ref] = {s_id: [tag, file]}

    @staticmethod
    def _sort_patients(patients):
        """
        Return a copy of patients with the samples in sorted order
        :param patients:
        :return:
        """

        if patients is None:
            return None

        ids2p_ref = dict()
        for p_ref, samples in patients.items():
            for _id in samples.keys():
                ids2p_ref[_id] = p_ref
        sorted_ids = list(ids2p_ref.keys())
        sorted_ids.sort()

        sorted_patients = dict()
        for _id in sorted_ids:
            p_ref = ids2p_ref[_id]
            if p_ref in sorted_patients:
                sorted_patients[p_ref][_id] = patients[p_ref][_id]
            else:
                sorted_patients[p_ref] = {_id: patients[p_ref][_id]}

        return sorted_patients

    @staticmethod
    def create_patients(command, patient_data, client_id):
        """
        :param command:
        :param patient_data:
        :param client_id:
        :return: dict of {patient_ref: (personalInformationId, medicalInformationId), . . . }
        """

        patient_refs = "--patient-ref=" + ",".join(patient_data.keys())

        # Build the create command
        print("Creating patients")
        create_command = command[:] + ["patient", "-c", patient_refs]

        if client_id:
            _ = AuthHelper.access_alt_client(create_command, client_id)
        else:
            result_create = run(create_command)
            _ = result_create.stdout
            Logger.log(result_create)

        # Build the list command
        print("Listing patients")
        list_command = command[:] + ["patient", "-l", patient_refs]

        if client_id:
            stdout_list = AuthHelper.access_alt_client(list_command, client_id)
        else:
            result_list = run(list_command)
            stdout_list = result_list.stdout
            Logger.log(result_list)

        return PatientHelper._extract_patients(stdout_list)

    @staticmethod
    def _extract_patients(stdout):
        # dict of IDs to return
        ids = None

        # Find the returned patient list and convert to Json
        patient_list = re.search(PatientHelper.ALL_PATIENTS_RE, stdout)
        if patient_list:
            try:
                print("Parsing IDs")
                result_json = json.loads(patient_list.groups()[0])
                ids = dict()
                for patient in result_json:
                    patient_ref = patient['userRef']
                    per_id = patient['personalInformationId']
                    med_id = patient['medicalInformationId']
                    ids[patient_ref] = (per_id, med_id)
            except json.decoder.JSONDecodeError:
                print("Error: couldn't decode patient IDs")
        else:
            print("Error: couldn't find patient list in stdout")

        return ids

    @staticmethod
    def is_valid(patient_data):
        """
        Checks that the patient data conforms to the expected format
        :param patient_data:
        :return:
        """

        def _is_mys(_tags):
            return "D" in _tags and "R" in _tags

        def _is_tumornormal(_tags):
            return "N" in _tags and "T" in _tags

        _valid = True

        for p_ref, samples in patient_data.items():
            probs = []

            # Check length of p_ref
            if len(p_ref) > PatientHelper.MAX_P_REF_LENGTH:
                probs.append(f"Patient refs can be no longer than {PatientHelper.MAX_P_REF_LENGTH} characters")

            tags = [samples[s_id][0] for s_id in samples.keys()]
            num_samples = len(samples)
            if num_samples == 2:
                # Should be DNA/RNA, Normal/Tumour, or unspecified
                if not _is_mys(tags) and not _is_tumornormal(tags) and tags != ["", ""]:
                    probs.append("Patients with two samples should have D and R (mys), T and N (tumorNormal), or none")
                    probs[-1] += f" - found <{tags[0]}> and <{tags[1]}>"
            elif num_samples == 1:
                # One sample should have D, R, or no tag
                if tags[0] not in ["", "D", "R"]:
                    probs.append(f"Single sample patients should be D, R, or empty - found {tags[0]}")
            else:
                probs.append(f"Each patient should have one or two samples - found {num_samples}")

            # If errors were found, inform the user
            if len(probs) > 0:
                _valid = False
                print(f"Error{'s' if len(probs) > 1 else ''} for patient ref {p_ref}")
                for p in probs:
                    print(p)
                print(json.dumps(patient_data[p_ref], indent=3))

        return _valid


class AuthHelper:

    @staticmethod
    def access_alt_client(command, client_id):
        """
        Authorise secondary client for patient command
        :param command:
        :param client_id:
        :return:
        """
        stdout = ""

        # Ask user for username/password and add to command
        user, password = AuthHelper._get_auth_info()
        command += ["--client-id", client_id, "-u", user, "-p", password]

        # Start uploader
        with subprocess.Popen(command, stdin=subprocess.PIPE, stdout=subprocess.PIPE) as auth_process:
            # Try to read in token prompt
            prompt = AuthHelper.get_prompt(auth_process)
            if AuthHelper.contains_coordinates(prompt):
                token = input(prompt)
                print("Sending token to subprocess")
                stdout, stderr = auth_process.communicate(token.encode())
                stdout = stdout.decode()
                if stderr:
                    print(f"stderr: {stderr}")

        return stdout

    @staticmethod
    def get_prompt(auth_process):
        """
        Read the prompt one byte at a time until it finds the colon character.
        Assumes the terminal colon is the only one. Current prompt is "Please enter token for coordinates [1, A]: "
        :param auth_process:
        :return:
        """

        prompt = ""
        bytes_to_read = 1

        next_char = auth_process.stdout.read(bytes_to_read).decode()
        if len(next_char) == bytes_to_read:
            while next_char != ":":
                prompt += next_char
                next_char = auth_process.stdout.read(bytes_to_read).decode()
        else:
            print("Error when reading prompt from subprocess")

        return f"{prompt}: "

    @staticmethod
    def _get_auth_info():
        """
        Ask user for username and password
        :return:
        """

        user = input("Please enter your username: ")
        password = getpass.getpass("Please enter your password: ")

        return user, password

    @staticmethod
    def contains_coordinates(prompt):
        """
        Return whether the line contains coordinates (e.g. [1, A]) to confirm we have the token prompt
        :param prompt:
        :return:
        """
        pattern = r"\[[1-8], [A-H]\]"
        match = re.search(pattern, prompt)
        return match


class UserHelper:
    @staticmethod
    def get_user_info(command):
        """
        :return: userId and clientID from userInfo command
        """

        user_id = -1
        client_id = -1

        # Execute `userInfo`
        print("Fetching userInfo")
        command += ["userInfo"]
        result = run(command)
        Logger.log(result)

        try:
            result_json = json.loads(result.stdout)
            if "userId" in result_json:
                user_id = result_json["userId"]
            if "clientId" in result_json:
                client_id = result_json["clientId"]
        except json.decoder.JSONDecodeError:
            print("Error: couldn't retrieve userInfo")

        return user_id, client_id

    @staticmethod
    def get_pipeline(command, pipeline_id):
        """
        If no pipeline_id provided, list those found and user chooses. If valid choice return associated sequencerId
        :return: pipelineId and sequencerId for provided pipeline_id (default to -1, -1)
        """
        sequencer_id = -1

        # Execute the `pipeline --list` command and get the output as Json
        print("Fetching available pipelines")
        command += ["pipeline", "--list"]
        result = run(command)
        result_json = json.loads(result.stdout)

        # A single pipeline found; put it in a list
        if isinstance(result_json, dict):
            result_json = [result_json]

        # Create map of pipeline_id -> (pipeline_name, sequencer_id)
        available_pipelines = dict()
        for pipeline in result_json:
            pid = pipeline["pipeline_id"]
            name = pipeline["pipeline_name"]
            seq = pipeline["sequencer_id"]
            available_pipelines[pid] = (name, seq)

        # Pipeline not specified on command line; ask user now
        if pipeline_id == -1:
            print("Multiple pipelines available")
            for pid, (name, _) in available_pipelines.items():
                print(f'{pid:5}: {name}')
            pipeline_id = input("Enter pipeline ID: ")

        # Convert the pipeline_id to int
        if pipeline_id.isnumeric():
            pipeline_id = int(pipeline_id)

        # Check available pipelines for specifed id
        for pid, (_, seq) in available_pipelines.items():
            if pipeline_id == pid:
                sequencer_id = seq
                break

        if sequencer_id == -1:
            print(f"Error: pipeline {pipeline_id} is invalid")

        return pipeline_id, sequencer_id


class JsonBuilder:
    # Map tags to analyses->definition->libraryType
    lib_type = {
        "R": "rna",
        "D": "dna", "N": "dna", "T": "dna",
        "": "dna"
    }

    # Map tags to topology->references->role
    role = {
        "D": "dna",
        "R": "rna",
        "N": "normal",
        "T": "tumor"
    }

    # Map tags to topology->definition->type
    # (R and T _should be_ redundant as the file names are sorted)
    top_type = {
        "D": "mys", "R": "mys",
        "N": "tumorNormal", "T": "tumorNormal"
    }

    @staticmethod
    def build_json(user_ref, user_id, client_id, pipeline_id, sequencer_id, patient_data, patient_ids, sample_type_id):
        """
        :param user_ref:
        :param user_id:
        :param client_id:
        :param pipeline_id:
        :param sequencer_id:
        :param patient_data:
        :param patient_ids:
        :param sample_type_id:
        :return: ADE format json object
        """
        ade_dict = {
            "protocolName": "ADE",
            "protocolVersion": "1",
            "client": {
                "id": client_id,
                "userId": user_id
            },
            "request": {
                "definition": {
                    "userRef": user_ref,
                    "sequencerId": sequencer_id,
                    "requestDate": int(time.time()),
                    "isPairedEnd": True,
                    "isPrevent": False
                },
                "state": None,
                "analyses": JsonBuilder._build_analyses(patient_data, patient_ids, pipeline_id, sample_type_id),
                "topology": JsonBuilder._build_topologies(patient_data),
                "files": []
            }
        }

        return json.dumps(ade_dict, indent=3)

    @staticmethod
    def _build_analyses(patient_data, patient_ids, pipeline_id, sample_type_id):
        """
        :param patient_data:
        :param pipeline_id:
        :return: list of analyses for ADE
        """
        analyses = []
        for patient_ref, samples in patient_data.items():
            for sample_id, data in samples.items():
                tag = data[0]
                personal_information_id, medical_information_id = patient_ids[patient_ref]
                analyses.append(
                    {
                        "definition": {
                            "sampleId": sample_id,
                            "multiplexId": sample_id,
                            "sgaPipelineId": pipeline_id,
                            "userRef": patient_ref if len(tag) == 0 else f"{patient_ref}-{tag}",
                            "sampleTypeId": sample_type_id,
                            "libraryType": JsonBuilder.lib_type[tag],
                        },
                        "patient": {
                            "personalInformationId": personal_information_id,
                            "medicalInformationId": medical_information_id
                        },
                        "isControlSample": False,
                        "files": [
                            {
                                "definition": {
                                    "name": file
                                },
                            } for file in data[1:]
                        ]
                    }
                )

        return analyses

    @staticmethod
    def _build_topologies(patient_data):
        """
        :param patient_data:
        :return: list of topologies for ADE
        """
        topologies = []
        for patient_ref, samples in patient_data.items():
            tags = [val[0] for val in samples.values()]
            tags.sort()
            # Only create a topology if there's a pair of D/R or N/T samples
            if len(tags) != 2 or (tags != ["D", "R"] and (tags != ["N", "T"])):
                continue
            topologies.append(
                {
                    "definition": {
                        "type": JsonBuilder.top_type[tags[0]]
                    },
                    "references": [
                        {
                            "analysisReference": {
                                "sampleId": sample_id
                            },
                            "role": JsonBuilder.role[data[0]],
                            "metadata": {}
                        }
                        for sample_id, data in samples.items()
                    ]
                }
            )

        return topologies


class Logger:

    @staticmethod
    def log(result):
        """
        Print the stdout and stderr of result if not empty
        :param result: return value of subprocess.run()
        :return:
        """
        out = result.stdout.strip()
        if len(out) > 0:
            print(out)

        err = result.stderr.strip()
        if len(err) > 0:
            print(err)


def confirm():
    """
    Dispplay warnings/info and return whether user wishes to continue
    :return:
    """
    print("Prerequisites")
    print(" - be logged in with sg-upload-v2-latest.jar")
    print(" - all files in target folder should use the same pipeline")
    print("Please do not upload any files containing nominative information or any other direct identifier ", end="")
    print("related to a patient (e.g. patient’s first and/or last names in file name)")

    return input("Do you wish to continue? (y/n) ").strip()[0].lower() == "y"


def run(command):
    global debug
    if debug:
        print("[DEBUG] Command to be run:", command)
    return subprocess.run(command, capture_output=True, text=True)


class RegexHelper:

    ILLUMINA_RE = r"^([^_]+?)(-([DRTN]))?_(S[1-9][0-9]*)_L\d+_R\d+_\d+\.fastq\.gz$"
    ILLUMINA_BREAKDOWN = ["0", "3", "2"]

    def __init__(self, filename):
        if filename is None:
            # Default to Illumina
            self.exprs = [(RegexHelper.ILLUMINA_RE, RegexHelper.ILLUMINA_BREAKDOWN)]
        else:
            self.exprs = RegexHelper.load_expressions(filename)

        self.functions = RegexHelper.build_functions(self.exprs)

    def update(self, filename, patients):
        updated = False
        for _func in self.functions:
            updated = _func(filename, patients)
            if updated:
                break
        return updated

    @staticmethod
    def load_expressions(file):
        exprs = []
        with open(file, "r") as file_in:
            lines = file_in.readlines()

        for _i in range(0, len(lines), 2):
            regex = lines[_i].strip()
            breakdown = lines[_i + 1].strip().split(" ")
            exprs.append((regex, breakdown))

        return exprs

    @staticmethod
    def build_functions(exprs):
        funcs = []

        for _expr in exprs:
            _regex = re.compile(_expr[0])
            _p_ref_ex = _expr[1][0]
            _mid_ex = _expr[1][1]
            _tag_ex = _expr[1][2] if len(_expr[1]) == 3 else None
            funcs.append(RegexHelper._build_function(_regex, _p_ref_ex, _mid_ex, _tag_ex))

        return funcs

    @staticmethod
    def _build_function(regex, p_ref_ex, mid_ex, tag_ex):
        def _func(filename, patients):
            basename = os.path.basename(filename)
            match = re.search(regex, basename)
            if match:
                p_ref = "".join([match.groups()[int(_c)] if _c.isdigit() else _c for _c in p_ref_ex])
                mid = "".join([match.groups()[int(_c)] if _c.isdigit() else _c for _c in mid_ex])
                tag = ""
                try:
                    tag = "".join([match.groups()[int(_c)] if _c.isdigit() else _c for _c in tag_ex])
                except TypeError:
                    # List comprehension evaluates to [None] for Illumina without a tag, so ignore this
                    pass

                PatientHelper.update_patient(patients, p_ref, mid, tag, filename)
                return True
            else:
                return False

        return _func


if __name__ == "__main__":
    # Parse the command line args
    _parser = argparse.ArgumentParser(description="Generate ADE file from FastQ folder")
    _parser.add_argument("folder", help="Path to a folder containing FastQ files")
    _parser.add_argument("-j", "--jar", default="./sg-upload-v2-latest.jar",
                         help="Location of sg-upload-v2-latest.jar (defaults to ./sg-upload-v2-latest.jar)")
    _parser.add_argument("-o", "--output", help="Output Json file (overwites without warning)")
    _parser.add_argument("-r", "--ref", help="A name for the run")
    _parser.add_argument("-p", "--pipeline", default=-1, help="ID of pipeline")
    _parser.add_argument("-s", "--sampletype", default=108000,
                         help="sampleTypeId to apply to all samples - defaults to 108000 (Peripheral Blood)")
    _parser.add_argument("-c", "--confirm", action="store_true", help="Confirm use of script")
    _parser.add_argument("-i", "--clientId", help="Client ID for the data")
    _parser.add_argument("-d", "--deep", action="store_true", help="Recurse through target folder")
    _parser.add_argument("-v", "--verbose", action="store_true", help="Debug mode")
    _parser.add_argument("-x", "--regex", help="Override regex")
    _parser.add_argument("-y", "--yaml", help="An override file for the CLI")
    _parser.set_defaults(verbose=False)
    _args = _parser.parse_args()

    if _args.verbose:
        debug = True
        print("[DEBUG] Debug mode active")

    # Get user confirmation to continue
    if not _args.confirm and not confirm():
        exit(0)

    # Make sure we have the upload CLI and set the upload command
    JAR_COMMAND = ["java", "-jar"]
    if os.path.exists(_args.jar):
        if _args.yaml:
            if os.path.exists(_args.yaml):
                JAR_COMMAND += [f"-Dmicronaut.config.files={_args.yaml}"]
            else:
                print(f"Couldn't find {_args.yaml}")
                exit(7)
        JAR_COMMAND += [_args.jar]
        print(JAR_COMMAND)
    else:
        print(f"Error: {_args.jar} not found")
        exit(6)

    # Parse the patient data from files in fastqfolder
    _patient_data = PatientHelper.read(os.path.join(os.getcwd(), _args.folder), _args.deep, _args.regex)
    if _patient_data is None or len(_patient_data) == 0:
        print("No patient data found")
        exit(1)
    elif not PatientHelper.is_valid(_patient_data):
        exit(2)

    # Get a dict of {patient_ref: (personalID, medicalId), . . . } for each patient
    _patients_ids = PatientHelper.create_patients(JAR_COMMAND[:], _patient_data, _args.clientId)
    if _patients_ids is None:
        exit(3)

    # Get user info
    _user_id, _client_id = UserHelper.get_user_info(JAR_COMMAND[:])
    if _args.clientId:
        _client_id = int(_args.clientId)
    if _user_id == -1 or _client_id == -1:
        exit(4)

    # Get pipeline info
    _pipeline_id, _sequencer_id = UserHelper.get_pipeline(JAR_COMMAND[:], _args.pipeline)
    if _sequencer_id == -1:
        exit(5)

    # Get userRef
    if _args.ref:
        _user_ref = _args.ref
    else:
        _user_ref = f'{pathlib.PurePath(_args.folder).name}_{datetime.datetime.now().strftime("%Y%m%d%H%M")}'

    # Put the ADE together
    _ade_json = JsonBuilder.build_json(_user_ref, _user_id, _client_id,
                                       _pipeline_id, _sequencer_id, _patient_data, _patients_ids, _args.sampletype)

    # Write to file if given, otherwise print to console
    if _args.output:
        save_path = os.path.join(os.getcwd(), _args.output)
        with open(save_path, "w") as f_out:
            f_out.write(_ade_json)
        print(f"Json written to {save_path}")
    else:
        print(_ade_json)
