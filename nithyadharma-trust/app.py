from flask import Flask, render_template, request, redirect, url_for, flash, g
import sqlite3
import datetime
import os
from collections import defaultdict 
import json
import random
import shutil
from PIL import Image # Import the Pillow library

app = Flask(__name__)
app.secret_key = 'your_secret_key_here' # Needed for flash messages

var_file_dir = os.path.join(app.root_path, 'var')
# variable file path so that generated whatsapp images and text goes
var_file_path = os.path.join(var_file_dir, 'whatsapp_messages.txt')

# Ensure DB_NAME points to the correct path where your database is located
DB_NAME = '/Users/sriram/Desktop/yard/rest/bin/database/temples.db'

TABLES = [
    'nd_donors', 'nd_donation', 'nd_expense_type', 'nd_expense', 
    'nd_kainkaryam' , 'nd_profile'
]

# Map table names to their ID columns for correct sorting (Newest first, using ID DESC)
SORT_MAP = {
    'nd_donation': 'don_id', # Sorting by Donation ID
    'nd_expense': 'exp_id',   # Sorting by Expense ID
    'nd_kainkaryam': 'k_id', # Assuming 'k_id' for Kainkaryam ID
}

# --- Jinja Filter Registration ---
def format_currency_filter(value):
    """Formats an integer with commas for thousands separator."""
    if value is None:
        return '0'
    try:
        # Converts to int, then formats with commas
        return "{:,}".format(int(value))
    except (ValueError, TypeError):
        return str(value)

# Register the function as a Jinja filter
app.jinja_env.filters['format_currency'] = format_currency_filter


# --- Database and Utility Functions ---

def get_db_connection():
    if 'conn' not in g:
        # Check if the database file exists before connecting (optional but helpful for debugging)
        if not os.path.exists(DB_NAME):
            print(f"Error: Database file not found at {DB_NAME}")
        g.conn = sqlite3.connect(DB_NAME)
        g.conn.row_factory = sqlite3.Row  # This allows accessing columns by name
    return g.conn

@app.teardown_appcontext
def close_db(e=None):
    conn = g.pop('conn', None)
    if conn is not None:
        conn.close()

def safe_int_conversion(value):
    if value is not None and str(value).strip():
        try:
            return int(value)
        except ValueError:
            print(f"Warning: Could not convert non-numeric value '{value}' to int.")
            return 0
    return 0

def get_header_data():
    conn = get_db_connection()
    profile_dict = {}
    
    # 1. Fetch current balances
    try:
        profile = conn.execute("SELECT bank_bal, petty_cash_bal FROM nd_profile LIMIT 1").fetchone()
        profile_dict = dict(profile) if profile else {}
        
        pcbal = safe_int_conversion(profile_dict.get('petty_cash_bal', 0))
        bbal = safe_int_conversion(profile_dict.get('bank_bal', 0))

    except sqlite3.OperationalError as e:
        print(f"!!! DATABASE ERROR: {e}. Defaulting balances to 0.")
        pcbal, bbal = 0, 0
    
    # 2. Total Donation and Total Expenses (Purchases)
    # Total Donation received (not just balance)
    total_donation = conn.execute("SELECT COALESCE(SUM(amt), 0) FROM nd_donation").fetchone()[0]

    # Total spending/purchases recorded in nd_expense (amt column is the total expense amount)
    purchase_bal = conn.execute("SELECT COALESCE(SUM(amt), 0) FROM nd_expense").fetchone()[0]

    # Total liquid assets (Current Balances , petty cash and bank balances)
    total_liquid_assets = pcbal + bbal
    
    # final total of all spending and balances (Total Expense as per user's previous logic)
    total_expense = total_liquid_assets + purchase_bal

    # Discrepancy based on Total Donations vs. Total Expenses
    discrepancy = total_donation - total_expense

    return {
        'pcbal': pcbal,
        'bbal': bbal,
        'purchase_bal': purchase_bal,
        'total_liquid_assets': total_liquid_assets, # New calculated field
        'total_expense': total_expense, # Total of all cash/bank/stock balances + purchases
        'total_donation': total_donation,
        'discrepancy': discrepancy
    }

import os
import random
import shutil

def copy_random_temple_image(dm_code: str, base_path: str = "/Users/sriram/Desktop/yard/temples/"):
    """
    Finds the directory associated with a DM code, selects a random file 
    from that directory, and copies it to the current working directory 
    as '{dm_code}.jpg'.

    Args:
        dm_code (str): The unique DM code (e.g., 'chakrapani').
        base_path (str): The root directory containing the temple folders.
                         Defaults to '/Users/sriram/Desktop/yard/temples/'.

    Returns:
        bool: True if a file was successfully copied, False otherwise.
    """
    # 1. Construct the target directory path
    # The requirement states the folder is '/Users/sriram/Desktop/yard/temples/kumbakonam-chakrapani'
    # We need to find the specific folder within the base_path that ends with '-{dm_code}'.
    
    target_dir_suffix = f"-{dm_code}"
    temple_dir = None

    print(f"Searching for directory ending in '{target_dir_suffix}' within: {base_path}")

    try:
        # List all items in the base path
        for item in os.listdir(base_path):
            full_path = os.path.join(base_path, item)
            # Check if it's a directory and if its name ends with the required suffix
            if os.path.isdir(full_path) and item.endswith(target_dir_suffix):
                temple_dir = full_path
                break
        
        if not temple_dir:
            print(f"Error: Temple directory for DM '{dm_code}' not found.")
            return False

        print(f"Found temple directory: {temple_dir}")
        
        # 2. Get list of files in the target directory
        all_files = os.listdir(temple_dir)
        
        # Filter out directories and hidden files (like .DS_Store on macOS)
        image_files = [f for f in all_files if os.path.isfile(os.path.join(temple_dir, f)) and not f.startswith('.')]
        
        if not image_files:
            print(f"Error: No image files found in {temple_dir}")
            return False

        # 3. Select a random file
        selected_file_name = random.choice(image_files)
        source_path = os.path.join(temple_dir, selected_file_name)
        
        # 4. Define the destination path in the current directory
        destination_file_name = f"{dm_code}.jpg"

        destination_path = os.path.join(var_file_dir, destination_file_name)

        # 4. Process the image using Pillow (PIL)
        print(f"Selected file: {selected_file_name}. Resizing to 640x480...")
        
        # Open the image file
        img = Image.open(source_path)
        
        # Resize the image to the specified dimensions (640x480)
        # We use Image.Resampling.LANCZOS for high quality downsampling
        resized_img = img.resize((640,480), Image.Resampling.BICUBIC)
        
        # 5. Save the resized image as the destination file
        resized_img.save(destination_path, 'JPEG') # Ensure output format is JPEG
        
        print(f"\nSuccessfully copied:")
        print(f"  Source: {source_path}")
        print(f"  Destination: {destination_path}")
        
        return True

    except FileNotFoundError:
        print(f"Error: Base path '{base_path}' does not exist.")
        return False
    except Exception as e:
        print(f"An unexpected error occurred: {e}")
        return False

# --- WhatsApp Message Generation ---
def generate_whatsapp_message(allocations, conn):
    """
    Generates a WhatsApp message collated by Temple Destination (DM).
    """
    
    # 1. Fetch maps for donor and temple names
    # Assuming 'temples' is the table name for temple data
    temple_map = {
        dm['dm']: {'name': dm['name'], 'place': dm['place']} 
        for dm in conn.execute("SELECT dm, name, place FROM temples where dm is not null").fetchall()
    }
    # Assuming 'nd_donors' exists and links to 'nd_donation'
    donation_map = {don['don_id']: don['donor'] for don in conn.execute("SELECT don_id, donor FROM nd_donation").fetchall()}
    
    # Structure to hold grouped data: {DM_KEY: {'donors': set(), 'comments': set()}}
    grouped_data = defaultdict(lambda: {'donors': set(), 'comments': set()})

    # 2. Process all allocations and group information by DM
    for alloc in allocations:
        dm = alloc['dm']
        comment = alloc['cmt']
        don_id = alloc['don_id']
        
        # pull any image from the folder using dm and copy it locally
        success = copy_random_temple_image(dm)

        if success:
            print("\ncopy_random_temple_image execution finished successfully for dm " + dm)
        else:
            print("\ncopy_random_temple_image execution failed for dm " + dm)
            
        donor_name = donation_map.get(don_id, f"Donor #{don_id}")
        
        grouped_data[dm]['donors'].add(donor_name)
        grouped_data[dm]['comments'].add(comment)

    # 3. Compile the final message content
    final_message = "--- Kainkaryam Allocation Report ---\n\n"
    
    for dm, data in grouped_data.items():
        # Retrieve the dictionary value from the map
        temple_info = temple_map.get(dm, {'name': f"Temple DM: {dm}", 'place': ''})
        
        # Combine name and place for display
        temple_display_name = temple_info['name']
        if temple_info['place']:
            temple_display_name += f" temple, {temple_info['place']}"

        # Format the list of donors (A, B, and C)
        donors = sorted(list(data['donors']))
        if len(donors) > 1:
            donor_list_str = f"{', '.join(donors[:-1])}, and {donors[-1]}"
        elif donors:
            donor_list_str = donors[0]
        else:
            donor_list_str = "A group of devotees" # Should not happen if data is processed correctly
        
        # Combine comments
        comments_list_str = " and ".join(sorted(list(data['comments'])))
        
        # Determine "part/entirety" phrasing (simple logic based on multiple donors/comments)
        contribution_phrase = "\nwith your collective contribution"
        if len(data['donors']) == 1 and len(data['comments']) == 1:
            contribution_phrase = "with your contribution"
            
        # Construct the message for this temple
        message_block = f"Dear {donor_list_str},\n"
        message_block += f"    {contribution_phrase}, the service(s):\n\n"
        message_block += f"    - '{comments_list_str}'\n\n"
        # Use the combined display name here
        message_block += f"    has been provided to \n\n"
        message_block += f"    *{temple_display_name}*.\n\n"
        message_block += "Thanks for your contribution and support of Dharma.\n\n"
        message_block += "Regards,\n\n"
        message_block += "NithyaDharma, Lokamotiv & TemplePages\n\n"
        # message_block += "--------------------------------------\n\n"
        
        final_message += message_block
        
    return final_message


# --- Reports Functions ---

def get_report_data(limit):
    conn = get_db_connection()
    reports = {}

    # 1. Total Temples Served & Total Allocation Amount
    total_metrics = conn.execute("""
        SELECT 
            COUNT(DISTINCT dm) as total_temples,
            COALESCE(SUM(amt), 0) as total_allocated_amount
        FROM nd_kainkaryam
    """).fetchone()
    reports['total_temples_served'] = safe_int_conversion(total_metrics['total_temples'])
    reports['total_allocated_amount'] = safe_int_conversion(total_metrics['total_allocated_amount'])
    
    # 2. Top X Donors by Amount Allocated
    # We join nd_kainkaryam with nd_donation to get the donor name.
    reports['top_donors'] = conn.execute(f"""
 		SELECT 
            T1.donor, 
            COALESCE(SUM(T1.amt), 0) as total_allocated 
        FROM nd_donation T1
        GROUP BY T1.donor
        ORDER BY total_allocated DESC
        LIMIT {limit}
    """).fetchall()

    # 3. Top X Temples by Amount Allocated
    # We join nd_kainkaryam with temples to get name, place, and DM.
    reports['top_temples'] = conn.execute(f"""
        SELECT 
            T1.dm, 
            T2.name AS name, 
            T2.place AS place, 
            COALESCE(SUM(T1.amt), 0) as total_allocated 
        FROM nd_kainkaryam T1
        JOIN temples T2 ON T1.dm = T2.dm
        GROUP BY T1.dm, T2.name, T2.place
        ORDER BY total_allocated DESC
        LIMIT {limit}
    """).fetchall()

    # 4. Split by Expense Type
    # We join nd_kainkaryam with nd_expense and nd_expense_type.
    # Note: nd_kainkaryam uses exp_id which links to nd_expense, and nd_expense uses 'type' which links to nd_expense_type
    reports['expense_split'] = conn.execute("""
		SELECT 
            T1.type, 
            COALESCE(SUM(T1.amt), 0) as total_allocated 
        FROM nd_expense T1
        GROUP BY T1.type
        ORDER BY total_allocated DESC
    """).fetchall()

    return reports


# --- Route 1: Table Viewer (Index) ---
@app.route('/', methods=['GET', 'POST'])
def index():
    conn = get_db_connection()
    
    # Use 'nd_donation' as the new default table if the user hasn't selected one
    default_table = 'nd_donation' if 'nd_donation' in TABLES else TABLES[0]
    table_name = request.form.get('table_select', default_table)
    table_data = []
    headers = []
    
    order_by_clause = ""
    sort_column = SORT_MAP.get(table_name)
    if sort_column:
        order_by_clause = f"ORDER BY {sort_column} DESC"

    if table_name in TABLES:
        try:
            query = f"SELECT * FROM {table_name} {order_by_clause}"
            cursor = conn.execute(query)
            table_data = cursor.fetchall()
            headers = [description[0] for description in cursor.description]
        except sqlite3.OperationalError as e:
            flash(f"Error viewing table {table_name}: {e}", 'error')
            
    header_data = get_header_data()
    
    return render_template('index.html', 
                           tables=TABLES, 
                           selected_table=table_name, 
                           headers=headers, 
                           data=table_data,
                           header_data=header_data)


# --- Route 2: Kainkaryam Entry (Transaction) ---
@app.route('/kainkaryam', methods=['GET', 'POST'])
def kainkaryam_entry():
    conn = get_db_connection()
    today_iso = datetime.date.today().isoformat()
    
    # Fetch Expenses with balance > 0
    expenses_db = conn.execute("""
        SELECT exp_id, dt, type, store, amt, bal, cmt 
        FROM nd_expense 
        WHERE bal > 0 
        ORDER BY exp_id DESC 
    """).fetchall()
    expenses = [dict(row) for row in expenses_db]

    # Fetching all active donations (bal > 0)
    donations_db = conn.execute("""
        SELECT don_id, donor, amt, bal, dt 
        FROM nd_donation 
        WHERE bal > 0 
        ORDER BY don_id ASC
    """).fetchall()
    donations_list = [dict(row) for row in donations_db]

    # Fetch Temple DM list
    try:
        temple_dms = conn.execute("SELECT dm, name, place FROM temples where dm is not null ORDER BY place ASC ").fetchall()
    except sqlite3.OperationalError:
        # Fallback if 'temples' table is missing
        temple_dms = [{'dm': 'DM_A', 'name': 'Temple A', 'place': 'City A'}] 

    header_data = get_header_data()
    
    def render_kainkaryam_page(form_data_dict=None):
        if form_data_dict is None:
            form_data_dict = {}
        return render_template('kainkaryam_entry.html', 
                               expenses=expenses, 
                               donations=donations_list, 
                               temple_dms=temple_dms,
                               default_date=today_iso,
                               form_data=form_data_dict, 
                               header_data=header_data)


    if request.method == 'POST':
        expense_id = request.form.get('expense_select')
        total_allocated_amount = 0
        allocations_to_process = []
        errors = []

        # 1. Iterate through donations to gather individual allocations
        for don in donations_list:
            don_id = don['don_id']
            
            # Form field names based on the HTML structure
            allocation_key = f'allocation_{don_id}'
            date_key = f'kainkaryam_dt_{don_id}'
            dm_key = f'temple_dm_select_{don_id}'
            comment_key = f'comments_{don_id}'
            
            # Retrieve values for this specific donation
            allocated_amt_str = request.form.get(allocation_key, '0')
            kainkaryam_date = request.form.get(date_key)
            expense_dm = request.form.get(dm_key)
            cmt = request.form.get(comment_key)

            try:
                allocated_amt = int(allocated_amt_str)
            except ValueError:
                allocated_amt = 0

            if allocated_amt > 0:
                # Basic validation for mandatory per-donation fields
                if not kainkaryam_date:
                    errors.append(f"Date is required for Donation #{don_id}.")
                if not expense_dm:
                    errors.append(f"Temple Destination is required for Donation #{don_id}.")
                if not cmt:
                    # Based on prior context, comment seems mandatory for WhatsApp message
                    errors.append(f"Comment is required for Donation #{don_id} for the WhatsApp message generation.")
                
                # Check against donation balance
                if allocated_amt > safe_int_conversion(don['bal']):
                    errors.append(f"Allocation (₹{allocated_amt}) for Donation #{don_id} exceeds its balance (₹{don['bal']}).")
                
                # If valid, add to processing list
                allocations_to_process.append({
                    'don_id': don_id,
                    'amt': allocated_amt,
                    'dt': kainkaryam_date,
                    'dm': expense_dm,
                    'cmt': cmt
                })
                total_allocated_amount += allocated_amt

        # 2. Expense-level validation
        selected_expense = conn.execute("SELECT bal FROM nd_expense WHERE exp_id = ?", (expense_id,)).fetchone()
        
        if total_allocated_amount == 0:
            errors.append("Please allocate an amount from at least one donation.")

        expense_balance = 0
        if expense_id and selected_expense:
            expense_balance = safe_int_conversion(selected_expense['bal'])
            if total_allocated_amount > expense_balance:
                errors.append(f"Total allocated amount (₹{total_allocated_amount}) exceeds the Expense Balance (₹{expense_balance}).")
        elif not expense_id:
            errors.append("Please select an Expense to fund.")

        if errors:
            for error in errors:
                flash(error, 'error')
            return render_kainkaryam_page(request.form)
        
        # 3. If no errors, proceed with transaction
        try:
            # A. Update Expense Balance
            new_expense_bal = expense_balance - total_allocated_amount
            conn.execute("UPDATE nd_expense SET bal = ? WHERE exp_id = ?", (new_expense_bal, expense_id))
            
            # B. Process Allocations, Update Donation Balances, and Insert Kainkaryam
            for alloc in allocations_to_process:
                # Fetch current balance again (for safety)
                current_don_bal_value = conn.execute("SELECT bal FROM nd_donation WHERE don_id = ?", (alloc['don_id'],)).fetchone()['bal']
                current_don_bal = safe_int_conversion(current_don_bal_value)

                # Update Donation Balance
                new_don_bal = current_don_bal - alloc['amt']
                conn.execute("UPDATE nd_donation SET bal = ? WHERE don_id = ?", (new_don_bal, alloc['don_id']))
                
                # Insert into nd_kainkaryam (using individual fields for each record)
                conn.execute("""
                    INSERT INTO nd_kainkaryam (k_dt, dm, amt, don_id, cmt, exp_id, credt) 
                    VALUES (?, ?, ?, ?, ?, ?, DATETIME('now', '+5 hours', '+30 minutes'))
                """, (alloc['dt'], alloc['dm'], alloc['amt'], alloc['don_id'], alloc['cmt'], expense_id))
            
            conn.commit()
            
            # C. Generate and Save WhatsApp Message
            whatsapp_content = generate_whatsapp_message(allocations_to_process, conn)

            # Write to a file in the var sub directory of app.py
            # Note: This file will overwrite on every submission.
            
            with open(var_file_path, 'a', encoding='utf-8') as f:
                f.write(whatsapp_content)
                
            # KEY CHANGE: Using 'success' category for green background
            flash(f"Kainkaryam entry complete. {len(allocations_to_process)} transactions processed. WhatsApp message saved to whatsapp_messages.txt.", 'success')
            return redirect(url_for('kainkaryam_entry')) 

        except Exception as e:
            conn.rollback()
            flash(f"Transaction failed: {e}", 'error')
            return render_kainkaryam_page(request.form)
        
    return render_kainkaryam_page()

# --- 1. Custom JSON Encoder (Place this at the top level of app.py) ---
class CustomJsonEncoder(json.JSONEncoder):
    """
    Custom JSONEncoder to handle non-standard types like sqlite3.Row and datetime objects.
    """
    def default(self, obj):
        # Handle sqlite3.Row objects by converting them to a standard Python dictionary
        if isinstance(obj, sqlite3.Row):
            return dict(obj)
        
        # Handle datetime objects (date and datetime) by converting them to ISO format strings
        if isinstance(obj, (datetime.date, datetime.datetime)):
            return obj.isoformat()

        # Let the base class handle other types
        return json.JSONEncoder.default(self, obj)


# --- Route 3: Reports Screen ---
@app.route('/reports', methods=['GET'])
def reports():
    """Renders the reports page with comprehensive report data, handling JSON serialization."""
    
    # Get user-selected limit, default to 10
    limit = safe_int_conversion(request.args.get('limit', 10))
    if limit not in [5, 10, 20]:
        limit = 10 
        
    # NOTE: Assuming get_header_data() and get_report_data() are defined elsewhere
    header_data = get_header_data()
    reports_data = get_report_data(limit)
    
    # CRITICAL FIX: Use the CustomJsonEncoder here (cls=CustomJsonEncoder)
    # This tells json.dumps how to serialize database Row objects and dates.
    try:
        reports_data_json_str = json.dumps(reports_data, cls=CustomJsonEncoder)
    except Exception as e:
        # Fallback if serialization still fails
        reports_data_json_str = json.dumps({}) 
        print(f"Failed to serialize reports data even with CustomJsonEncoder: {e}")
        
    
    # Pass all required variables to the template
    return render_template(
        'reports.html', 
        reports_data=reports_data, # For table rendering in Jinja
        header_data=header_data,
        limit=limit,
        reports_data_json_str=reports_data_json_str # The correctly serialized data string for JavaScript
    )

if __name__ == '__main__':
    app.run(debug=True)