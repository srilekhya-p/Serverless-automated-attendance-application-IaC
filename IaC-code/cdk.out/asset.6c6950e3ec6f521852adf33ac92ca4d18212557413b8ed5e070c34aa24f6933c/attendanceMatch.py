import json
import boto3

# AWS Clients
s3 = boto3.client('s3')
rekognition = boto3.client('rekognition')
textract = boto3.client('textract')
dynamodb = boto3.resource('dynamodb')

# Constants
S3_BUCKET_NAME = "proj3-uco-bucket"
CLASSROOM_FOLDER_PATH = "classroom/"
DYNAMODB_TABLE_NAME = "proj3-records"

def lambda_handler(event, context):
    try:
        # Extract input parameters from event
        user_image_s3_path = event['userImageS3Path']
        user_name = event['userName']
        email = event['email']
        class_date = event['classDate']

        # List objects in the classroom folder
        response = s3.list_objects_v2(Bucket=S3_BUCKET_NAME, Prefix=CLASSROOM_FOLDER_PATH)
        if 'Contents' not in response:
            return {"statusCode": 400, "body": "No images found in S3 folder."}

        faces_images = []
        names_images = []

        # Categorize images based on prefix
        for obj in response['Contents']:
            image_key = obj['Key']
            if image_key.startswith(CLASSROOM_FOLDER_PATH + "faces"):
                faces_images.append(image_key)
            elif image_key.startswith(CLASSROOM_FOLDER_PATH + "names"):
                names_images.append(image_key)

        # Step 1: Check face match using Rekognition
        matched_face = None
        for face_img in faces_images:
            print("face_ img: "+face_img)
            if compare_faces(user_image_s3_path, face_img):
                matched_face = face_img
                break  # Stop on first match

        # Step 2: Check name match using Textract
        matched_name = None
        for name_img in names_images:
            extracted_names = extract_text_from_image(name_img)
            if user_name.lower() in extracted_names:
                matched_name = name_img
                break  # Stop on first match

        # Step 3: Determine participation (face or name match)
        participation = matched_face is not None or matched_name is not None

        # Step 4: Save participation record to DynamoDB
        save_to_dynamodb(user_name, email, class_date, participation)

        return {
            "statusCode": 200,
            "body": json.dumps({
                "matched_face": matched_face,
                "matched_name": matched_name,
                "participation": participation
            })
        }

    except Exception as e:
        return {
            "statusCode": 500,
            "body": json.dumps({"error": str(e)})
        }

def compare_faces(user_image, classroom_image):
    """ Compares user image with each faces*.jpg using Rekognition """
    try:
        response = rekognition.compare_faces(
            SourceImage={'S3Object': {'Bucket': S3_BUCKET_NAME, 'Name': user_image.replace("s3://"+S3_BUCKET_NAME+"/", "")}},
            TargetImage={'S3Object': {'Bucket': S3_BUCKET_NAME, 'Name': classroom_image.replace("s3://"+S3_BUCKET_NAME+"/", "")}},
            SimilarityThreshold=80
        )
        return len(response.get("FaceMatches", [])) > 0
    except Exception as e:
        print(f"Rekognition error: {e}")
        return False


def extract_text_from_image(image_key):
    """ Extracts text from names*.jpg using Textract """
    try:
        response = textract.detect_document_text(
            Document={'S3Object': {'Bucket': S3_BUCKET_NAME, 'Name': image_key}}
        )
        return " ".join([item["Text"] for item in response.get("Blocks", []) if item["BlockType"] == "WORD"]).lower()
    except Exception as e:
        print(f"Textract error: {e}")
        return ""

def save_to_dynamodb(name, email, class_date, participation):
    """ Saves participation record to DynamoDB """
    table = dynamodb.Table(DYNAMODB_TABLE_NAME)
    table.put_item(
        Item={
            "name": name,
            "email": email,
            "classdate": class_date,
            "participation": participation
        }
    )
