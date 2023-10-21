# sg-upload-v2-wrapper.py is a wrapper script for uploader utility jar written in python3
# Wrapper script is recommended way of using uploader utility to automatically stay up-to-date in terms of latest bugs
# and security updates
# All updates are retro-compatible in terms of command interfaces.
# Note: script uses only python3 standard library and doesn't depend on external packages.

# How it works:

# Prerequisite: previously downloaded uploader utility

# 1. If no uploader previously downloaded uploader utility found (`sg-upload-v2-latest.jar`  in current directory) then
# downlods latest version of uploader, if already present then updates local sg-upload-v2-latest.jar with the latest
# version which includes latest bug and security fixes
# 2. Transparently redirects any command arguments to uploader jar so can be integrated in any existing scripts without
# disruption  simply by replacing 'java -jar sg-upload-v2-latest.jar' with 'python3 sg-upload-v2-wrapper.py'


# Example usage :
# python3 sg-upload-v2-wrapper.py login -u some -p secret
# python3 sg-upload-v2-wrapper.py new --legacy -j some-bar.json
# python3 sg-upload-v2-wrapper.py status --id 200005105
# etc.


import urllib.request
import hashlib
import sys
import subprocess
import ssl

# In case of "[SSL: CERTIFICATE_VERIFY_FAILED] certificate verify faile" error please uncomment the following line
# see https://support.sectigo.com/articles/Knowledge/Sectigo-AddTrust-External-CA-Root-Expiring-May-30-2020
# ssl._create_default_https_context = ssl._create_unverified_context


remote_url = "https://ddm.sophiagenetics.com/direct/sg/uploaderv2"
uploader_filename = "sg-upload-v2-latest.jar"
upload_checksum_filename = "sg-upload-v2-latest.jar.md5"


def get_remote_checksum():
    url = "%s/%s" % (remote_url, upload_checksum_filename)
    try:
        response = urllib.request.urlopen(url)
        if response.status != 200:
            print("ERR. Remote version not found: %s " % url)
            sys.exit(1)
        md5sum = response.read().decode('utf-8')
        return md5sum
    except Exception as err:
        print("ERROR: Problem connecting to %s " % url)
        print("ERROR: %s " % err)
        return ""


def get_current_checksum():
    hash_md5 = hashlib.md5()
    try:
        with open(uploader_filename, "rb") as f:
            for chunk in iter(lambda: f.read(4096), b""):
                hash_md5.update(chunk)
        return hash_md5.hexdigest()
    except FileNotFoundError:
        return ""  # return empty checksum if no file found


def update_self():
    update_url = "%s/%s" % (remote_url, uploader_filename)
    response = urllib.request.urlopen(update_url)
    me = open(uploader_filename, 'wb')
    me.write(response.read())
    me.close()


def main():
    remote_checksum = get_remote_checksum()

    if remote_checksum == "":
        print("WARN. No new version found. Using previous one!")
    else:
        current_checksum = get_current_checksum()
        if current_checksum == "":
            print("Downloading latest uploader version. Checksum: {0}.".format(remote_checksum))
            update_self()
        else:
            remote_checksum = remote_checksum.strip()
            current_checksum = current_checksum.strip()
            if remote_checksum != current_checksum:
                print("Current checksum: %s" % current_checksum)
                print("Remote checksum: %s" % remote_checksum)
                update_self()
                print("Updated to version {0}.".format(remote_checksum))
            else:
                print("Script is up-to-date (checksum {0})".format(remote_checksum))

    cmd = build_command(sys.argv)

    _ = subprocess.run(cmd)


def build_command(args: list):
    """
    The override should be the first option (index 1)
    """
    command = ['java', '-jar']
    if "-Dmicronaut.config.files=" in args[1]:
        command += [args[1]] + [uploader_filename] + args[2:]
    else:
        command += [uploader_filename] + args[1:]
    return command


if __name__ == "__main__":
    main()
